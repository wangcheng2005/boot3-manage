package org.github.zuuuyao.service.impl;

import cn.hutool.core.collection.CollUtil;
import org.github.zuuuyao.entity.FillQuestionAnswerOptionItem;
import org.github.zuuuyao.entity.QuestionAnswerDetail;
import org.github.zuuuyao.entity.QuestionAnswerOptionItem;
import org.github.zuuuyao.entity.QuestionEntity;
import org.github.zuuuyao.entity.enums.QuestionAnswerTypeEnums;
import org.github.zuuuyao.entity.enums.QuestionTypeEnums;
import org.github.zuuuyao.playwright.Question;
import org.github.zuuuyao.repository.QuestionRepository;
import org.github.zuuuyao.service.QuestionImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class QuestionImportServiceImpl implements QuestionImportService {

    private final QuestionRepository questionRepository;

    public QuestionImportServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveFetchedQuestions(List<Question> questions) {
        if (CollUtil.isEmpty(questions)) return 0;
        int count = 0;
        for (Question q : questions) {
            QuestionEntity entity = mapToEntity(q);
            if (entity != null) {
                questionRepository.insert(entity);
                count++;
            }
        }
        return count;
    }

    /**
     * 将爬取到的 Question 映射为 QuestionEntity。
     * 规则：
     * - describe -> content
     * - difficultyName -> difficulty (根据名称/值映射；未知置空)
     * - examable -> isReal (考试题)；exercise -> isPractice；boutique -> isEssence
     * - options/answers -> QuestionAnswerDetail（基础结构，选项按 originOrder/answerSeq 排序）
     */
    private QuestionEntity mapToEntity(Question q) {
        if (q == null) return null;
        QuestionEntity e = new QuestionEntity();
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        // content
        e.setContent(q.describe);

        // type/answerType: 粗略映射，依据字段 attribute/difficultyName 里常见值；可根据真实数据再细化
        QuestionTypeEnums type = guessType(q);
        if (type != null) e.setType(type.getCode());
        if (type != null && type.getQuestionAnswerTypeEnums() != null) {
            e.setAnswerType(type.getQuestionAnswerTypeEnums().getCode());
        }

        // difficulty
        Integer difficulty = mapDifficulty(q.difficultyName);
        if (difficulty != null) e.setDifficulty(difficulty);

        // flags
        e.setIsReal(q.examable ? 1 : 0);
        e.setIsPractice(q.exercise ? 1 : 0);
        e.setIsEssence(q.boutique ? 1 : 0);
        e.setIsEnglish(0);
        e.setHasImage(0);
        e.setStatus(1);       // enable
        e.setReviewStatus(1); // pass
        e.setIsReference(0);
        e.setMedicineType(null);
        e.setSystemType(null);
        e.setOrganizationId(null);
        e.setQuestionPermission(null);
        e.setReviewer(null);
        e.setReviewTime(null);
        e.setQuestionCategoryIds(Collections.emptyList());
        e.setQuestionLabelsIds(Collections.emptyList());
        e.setRemark(null);
        e.setParentId(null);
        e.setSort(null);
        e.setSubCount(null);

        // answers/options
        e.setAnswer(buildAnswerDetail(q));
        e.setExplanation(null);

        return e;
    }

    private QuestionTypeEnums guessType(Question q) {
        if (q == null) return null;
        // 根据属性/答案数量粗略判断：多选/单选
        if (CollUtil.isNotEmpty(q.answers) && q.answers.size() > 1) {
            return QuestionTypeEnums.INDEFINITE;
        }
        // 默认单选
        return QuestionTypeEnums.A1;
    }

    private Integer mapDifficulty(String difficultyName) {
        if (difficultyName == null) return null;
        String dn = difficultyName.trim().toLowerCase(Locale.ROOT);
        switch (dn) {
            case "简单":
            case "easy":
                return 1;
            case "中等":
            case "normal":
                return 2;
            case "困难":
            case "hard":
                return 3;
            default:
                return null;
        }
    }

    private QuestionAnswerDetail buildAnswerDetail(Question q) {
        if (q == null) return null;
        QuestionAnswerDetail detail = new QuestionAnswerDetail();

        // 判断是否选择题：有 options 列表即视为选择题
        if (CollUtil.isNotEmpty(q.options)) {
            detail.setType(QuestionAnswerTypeEnums.SINGLE.getCode());
            List<QuestionAnswerOptionItem> opts = new ArrayList<>();
            for (Question.Option o : q.options) {
                QuestionAnswerOptionItem item = new QuestionAnswerOptionItem();
                // value 用 originOrder 或 answerSeq
                if (o.originOrder != null) item.setValue(o.originOrder);
                else item.setValue(String.valueOf(o.answerSeq));
                item.setContent(o.describe);
                item.setCorrect(o.correct ? 1 : 0);
                opts.add(item);
            }
            detail.setOptions(opts);

            // 正确答案，取 options 中 correct==true 的 value
            List<String> answers = new ArrayList<>();
            for (QuestionAnswerOptionItem item : opts) {
                if (Objects.equals(item.getCorrect(), 1)) {
                    answers.add(item.getValue());
                }
            }
            detail.setAnswer(answers);
            return detail;
        }

        // 非选择题：填空或简答，先尝试从 answers 中拿数字/文本
        detail.setType(QuestionAnswerTypeEnums.TEXT.getCode());
        if (CollUtil.isNotEmpty(q.answers)) {
            List<String> answers = new ArrayList<>();
            for (Question.AnswerObj ao : q.answers) {
                if (ao.answer != null) answers.add(String.valueOf(ao.answer));
            }
            detail.setAnswer(answers);
        }
        return detail;
    }
}

