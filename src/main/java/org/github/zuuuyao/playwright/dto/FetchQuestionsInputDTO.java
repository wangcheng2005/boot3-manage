package org.github.zuuuyao.playwright.dto;

import lombok.Data;

import java.util.List;

@Data
public class FetchQuestionsInputDTO {
    /** 分类 id 列表，接口会将这些 categories 拼接为多个 categories 参数 */
    private List<String> categories;
}

