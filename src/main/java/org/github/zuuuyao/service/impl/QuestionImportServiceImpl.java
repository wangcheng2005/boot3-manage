package org.github.zuuuyao.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.github.zuuuyao.entity.*;
import org.github.zuuuyao.entity.enums.QuestionTypeEnums;
import org.github.zuuuyao.playwright.QuestionDetail;
import org.github.zuuuyao.repository.QuestionCategoryRepository;
import org.github.zuuuyao.repository.QuestionRepository;
import org.github.zuuuyao.service.QuestionImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuestionImportServiceImpl implements QuestionImportService {

    @Resource
    private QuestionRepository questionRepository;
    @Resource
    private QuestionCategoryRepository questionCategoryRepository;

    // directory to save downloaded images (relative to project root or absolute)
    private static final String IMAGE_SAVE_DIR = "/data/images";
    // base URL to serve images from (replace with your actual CDN/OSS URL)
    private static final String IMAGE_BASE_URL = "https://static.example.com/images";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveFetchedQuestions(List<QuestionDetail> questions) {
        if (CollUtil.isEmpty(questions)) {
            return 0;
        }
        // A1 A2 普通题, 单选
        // A3/A4 有子题
        // B型 C型 有子题, 共用选项
        // X型, 多选
        // 不定项选择题, 和X型一样, 多选
        // 案例分析题  带子题的多选




        int count = 0;
        for (QuestionDetail q : questions) {
            QuestionEntity entity = mapToEntity(q, false, null);
            if (entity != null) {
                questionRepository.insert(entity);
                count++;

                // 处理子题：直接插入子题并设置 parentId
                List<QuestionDetail> subQuestion = q.subQuestion;
                count += insertSubQuestions(entity, subQuestion);
            }
        }
        return count;
    }

    // 插入子题，返回插入数量
    private int insertSubQuestions(QuestionEntity parentId, List<QuestionDetail> subQuestion) {
        if (subQuestion == null || subQuestion.isEmpty()) {
            return 0;
        }
        int inserted = 0;
        for (QuestionDetail subQ : subQuestion) {
            QuestionEntity subE = mapToEntity(subQ, true, parentId);
            if (subE != null) {
                questionRepository.insert(subE);
                inserted++;
            }
        }
        return inserted;
    }

    /**
     * 将爬取到的 Question 映射为 QuestionEntity。
     * 规则：
     * - describe -> content
     * - difficultyName -> difficulty (根据名称/值映射；未知置空)
     * - examable -> isReal (考试题)；exercise -> isPractice；boutique -> isEssence
     * - options/answers -> QuestionAnswerDetail（基础结构，选项按 originOrder/answerSeq 排序）
     */
    private QuestionEntity mapToEntity(QuestionDetail q, boolean isSubQuestion, QuestionEntity parent) {
        if (q == null) {
            return null;
        }
        QuestionEntity e = new QuestionEntity();
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setCreator(1);
        e.setUpdater(1);
        // content + 图片处理
        String content = processContentImages(q, e);
        e.setContent(content);


        // 查询分类
        List<Integer> categoryList = new ArrayList<>();
        if (CollUtil.isNotEmpty(q.category)) {
            LambdaQueryWrapper<QuestionCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(QuestionCategoryEntity::getCategoryId, q.category);
            List<QuestionCategoryEntity> categories = questionCategoryRepository.selectList(queryWrapper);
            categoryList = categories.stream().map(QuestionCategoryEntity::getId).collect(Collectors.toList());
        }
        e.setQuestionCategoryIds(categoryList);
        e.setQuestionLabelsIds(Collections.emptyList());
        e.setSystemType(1);
        QuestionTypeEnums type = guessType(q, parent);
        e.setType(type.getCode());
        e.setAnswerType(type.getQuestionAnswerTypeEnums().getCode());
        e.setIsReal(1);
        e.setIsEssence(1);
        e.setIsPractice(1);
        e.setIsExam(1);
        e.setIsEnglish(0);

        int attribute = quessAttribute(q);
        e.setMedicineType(attribute);
        e.setQuestionPermission(1);
        e.setStatus(1);
        e.setReviewStatus(1);
        e.setReviewer(1);
        e.setReviewTime(LocalDateTime.now());
        e.setIsReference(0);
        // difficulty
        Integer difficulty = mapDifficulty(q.difficultyName);
        e.setDifficulty(difficulty);
        if (q.difficultyNumber != null) {
            // Double 乘以 10000 , 再转成 Integer, 保留小数精度
            Integer difficultyNumber = (int) Math.round(q.difficultyNumber * 10000.0);
            e.setDifficultyCoefficient(difficultyNumber);
        }

        // note: hasImage already set above based on q.describe
        e.setParentId(isSubQuestion ? parent.getId() : null);
        e.setSort(isSubQuestion ? q.orders : 0);
        e.setExplanation(q.analysis);
        e.setRemark(q.mark);

        // 处理子题
        List<QuestionDetail> subQuestion = q.subQuestion;
        e.setSubCount(subQuestion == null || isSubQuestion ? 0 : subQuestion.size());

        // 组装答案/选项
        e.setAnswer(buildAnswerDetail(q, type, parent));

        return e;
    }

    /**
     * 从 HTML 中提取 img src
     */
    private List<String> extractImageSrcs(String html) {
        if (html == null || html.isEmpty()) {
            return Collections.emptyList();
        }
        Pattern p = Pattern.compile("(?i)<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher m = p.matcher(html);
        List<String> list = new ArrayList<>();
        while (m.find()) {
            String src = m.group(1);
            if (src != null && !src.isEmpty()) {
                list.add(src);
            }
        }
        return list;
    }

    /**
     * 下载图片到本地并返回新的可访问 URL（简单拼接 IMAGE_BASE_URL）。
     * 如果失败则返回原 imageUrl。
     */
    private String downloadAndSaveImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }
        try {
            Path dir = Paths.get(IMAGE_SAVE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String ext = "";
            String path = new URL(imageUrl).getPath();
            int lastDot = path.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < path.length() - 1) {
                ext = path.substring(lastDot);
                if (ext.length() > 6) {
                    ext = "";
                }
            }
            String fileName = UUID.randomUUID() + (ext.isEmpty() ? ".jpg" : ext);
            Path target = dir.resolve(fileName);

            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, target);
            } finally {
                conn.disconnect();
            }

            return IMAGE_BASE_URL + "/" + fileName;
        } catch (Exception e) {
            return imageUrl;
        }
    }

    private int quessAttribute(QuestionDetail q) {
        if (q == null || q.attribute == null) {
            return 3;
        }
        return switch (q.attribute) {
            case "西医题" -> 2;
            case "中医题" -> 1;
            case "中西医题" -> 3;
            default -> 3;
        };
    }

    private QuestionTypeEnums guessType(QuestionDetail q, QuestionEntity parent) {
        if (parent != null) {
            return QuestionTypeEnums.parseByCode(parent.getType());
        }
        if (q == null || q.type == null) {
            return QuestionTypeEnums.A2;
        }
        String t = q.type;
        return switch (t) {
            case "A1" -> QuestionTypeEnums.A1;
            case "A2" -> QuestionTypeEnums.A2;
            case "A3/A4" -> QuestionTypeEnums.A3A4;
            case "B" -> QuestionTypeEnums.B;
            case "C" -> QuestionTypeEnums.C;
            case "X" -> QuestionTypeEnums.X;
            case "fill" -> QuestionTypeEnums.FILL;
            case "judge" -> QuestionTypeEnums.JUDGE;
            case "Q" -> QuestionTypeEnums.QA;
            case "explain" -> QuestionTypeEnums.EXPLAIN;
            case "brief" -> QuestionTypeEnums.BRIEF;
            case "case" -> QuestionTypeEnums.CASE;
            case "indefinite" -> QuestionTypeEnums.INDEFINITE;
            default -> QuestionTypeEnums.A2;
        };
    }

    private Integer mapDifficulty(String difficultyName) {
        if (difficultyName == null) {
            return null;
        }
        String dn = difficultyName.trim().toLowerCase(Locale.ROOT);
        return switch (dn) {
            case "简单", "easy" -> 1;
            case "中等", "common" -> 2;
            case "较难", "hard" -> 3;
            default -> null;
        };
    }

    private QuestionAnswerDetail buildAnswerDetail(QuestionDetail q, QuestionTypeEnums type, QuestionEntity parent) {
        if (q == null) {
            return null;
        }
        QuestionAnswerDetail detail = new QuestionAnswerDetail();
        detail.setType(type.getQuestionAnswerTypeEnums().getCode());

        // 问答题单独处理
        if (QuestionTypeEnums.QA == type || QuestionTypeEnums.EXPLAIN == type || QuestionTypeEnums.BRIEF == type) {
            detail.setFillAnswers(Collections.emptyList());
            detail.setOptions(Collections.emptyList());
            detail.setAnswer(Collections.emptyList());
            detail.setAnswer(Arrays.asList(q.answers));
            return detail;
        }

        // 判断题单独处理
        if (QuestionTypeEnums.JUDGE == type) {
            List<QuestionAnswerOptionItem> options = q.options.stream()
                    .map(it -> {
                        QuestionAnswerOptionItem item = new QuestionAnswerOptionItem();
                        item.setContent(it.describe);
                        item.setValue(it.correct ? "true" : "false");
                        item.setCorrect(it.correct ? 1 : 0);
                        return item;
                    }).collect(Collectors.toList());
            detail.setFillAnswers(Collections.emptyList());
            detail.setOptions(options);
            detail.setAnswer(Collections.emptyList());
            List<String> answers = options.stream().filter(it -> it.getCorrect() == 1)
                    .map(QuestionAnswerOptionItem::getValue)
                    .collect(Collectors.toList());
            detail.setAnswer(answers);
            return detail;
        }

        // 填空题单独处理
        if (QuestionTypeEnums.FILL == type) {
            List<FillQuestionAnswerOptionItem> options = q.options.stream()
                    .map(it -> {
                        FillQuestionAnswerOptionItem item = new FillQuestionAnswerOptionItem();
                        item.setIndex(it.answerSeq);
                        item.setValue(Arrays.asList(it.describe));
                        return item;
                    }).collect(Collectors.toList());
            detail.setOptions(Collections.emptyList());
            detail.setFillAnswers(options);
            detail.setAnswer(Collections.emptyList());
            return detail;
        }

        // B型题单独处理
        if ((QuestionTypeEnums.B == type || QuestionTypeEnums.C == type) && parent != null) {
            // 组装选项
            String answer = q.answers;
            List<QuestionAnswerOptionItem> options = parent.getAnswer().getOptions().stream()
                    .map(it -> {
                        QuestionAnswerOptionItem item = new QuestionAnswerOptionItem();
                        item.setContent(it.getContent());
                        item.setCorrect(answer.equals(it.getValue()) ? 1 : 0);
                        item.setValue(it.getValue());
                        return item;
                    }).collect(Collectors.toList());
            detail.setOptions(options);
            List<String> answers = options.stream().filter(it -> it.getCorrect() == 1)
                    .map(QuestionAnswerOptionItem::getValue)
                    .collect(Collectors.toList());
            detail.setAnswer(answers);
            detail.setFillAnswers(Collections.emptyList());
            return detail;
        }

        // 组装选项
        List<QuestionAnswerOptionItem> options = q.options.stream()
                .map(it -> {
                    QuestionAnswerOptionItem item = new QuestionAnswerOptionItem();
                    item.setContent(it.describe);
                    item.setCorrect(it.correct ? 1 : 0);
                    item.setValue(it.originOrder);
                    return item;
                }).collect(Collectors.toList());
        detail.setOptions(options);
        List<String> answers = options.stream().filter(it -> it.getCorrect() == 1)
                .map(QuestionAnswerOptionItem::getValue)
                .collect(Collectors.toList());
        detail.setAnswer(answers);
        detail.setFillAnswers(Collections.emptyList());
        return detail;
    }

    // 将 content 中的图片下载并替换为新 URL，同时设置 hasImage
    private String processContentImages(QuestionDetail q, QuestionEntity e) {
        String content = q.describe == null ? "" : q.describe;
        List<String> imgUrls = extractImageSrcs(content);
        if (imgUrls.isEmpty()) {
            e.setHasImage(0);
            return content;
        }
        e.setHasImage(1);
        for (String src : imgUrls) {
            try {
                String newUrl = downloadAndSaveImage(src);
                if (newUrl != null && !newUrl.isEmpty()) {
                    content = content.replace(src, newUrl);
                }
            } catch (Exception ignored) {
                // keep original src
            }
        }
        return content;
    }
}
