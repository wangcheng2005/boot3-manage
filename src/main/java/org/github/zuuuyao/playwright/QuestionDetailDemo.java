package org.github.zuuuyao.playwright;

import cn.hutool.json.JSONUtil;

import java.util.Map;

public class QuestionDetailDemo {
  public static void main(String[] args) {
    String json = "{\n" +
      "  \"id\":\"26012214150802014220265882505224\",\n" +
      "  \"type\":\"A2\",\n" +
      "  \"typeName\":\"A2型\",\n" +
      "  \"describe\":\"女性，30岁。妊娠3个月，出现面部红斑、关节疼痛，检查ANA 1∶320阳性，如果继续妊娠可以应用的药物是  (  )\",\n" +
      "  \"category\":[\"26012214124702014219676101697537\"],\n" +
      "  \"analysis\":null,\n" +
      "  \"answers\":\"D\",\n" +
      "  \"options\":[\n" +
      "    {\"correct\":false,\"answerSeq\":1,\"originOrder\":\"A\",\"describe\":\"环磷酰胺\",\"id\":\"26012214150802014220265882505225\",\"orders\":1},\n" +
      "    {\"correct\":false,\"answerSeq\":2,\"originOrder\":\"B\",\"describe\":\"硫唑蝶呤\",\"id\":\"26012214150802014220265882505226\",\"orders\":2},\n" +
      "    {\"correct\":false,\"answerSeq\":3,\"originOrder\":\"C\",\"describe\":\"甲氨蝶呤\",\"id\":\"26012214150802014220265882505227\",\"orders\":3},\n" +
      "    {\"correct\":true,\"answerSeq\":4,\"originOrder\":\"D\",\"describe\":\"羟氯喹\",\"id\":\"26012214150802014220265882505228\",\"orders\":4},\n" +
      "    {\"correct\":false,\"answerSeq\":5,\"originOrder\":\"E\",\"describe\":\"地塞米松\",\"id\":\"26012214150802014220265882505229\",\"orders\":5}\n" +
      "  ],\n" +
      "  \"difficulty\":\"easy\",\n" +
      "  \"difficultyName\":\"简单\",\n" +
      "  \"label\":[],\n" +
      "  \"createTime\":\"2026-01-26 22:56:47\",\n" +
      "  \"examed\":false,\n" +
      "  \"boutique\":false,\n" +
      "  \"examable\":true,\n" +
      "  \"exercise\":false,\n" +
      "  \"languages\":false,\n" +
      "  \"attribute\":\"西医题\",\n" +
      "  \"cognitionLevel\":null,\n" +
      "  \"answerKey\":{\"common\":[],\"core\":[]},\n" +
      "  \"answersObject\":[{\"id\":\"26012214150802014220265882505230\",\"bankId\":\"26012214150802014220265882505224\",\"subquestionId\":null,\"answer\":4,\"orders\":1}],\n" +
      "  \"qsCategoriesName\":[{}],\n" +
      "  \"bankId\":\"26012214150802014220265882505224\",\n" +
      "  \"questionBankId\":\"\",\n" +
      "  \"useType\":\"考试题,西医题\",\n" +
      "  \"difficultyNumber\":0.1,\n" +
      "  \"mark\":\"无\",\n" +
      "  \"cateids\":null,\n" +
      "  \"isFeedback\":0,\n" +
      "  \"handledErrorCount\":0,\n" +
      "  \"allErrorCount\":0,\n" +
      "  \"subQuestion\":[],\n" +
      "  \"answerPoints\":[]\n" +
      "}";

    Map<?, ?> map = JSONUtil.toBean(json, Map.class);
    QuestionDetail qd = new QuestionDetail(map);

    System.out.println("Parsed QuestionDetail: " + qd);
    System.out.println("id=" + qd.id);
    System.out.println("describe=" + qd.describe);
    System.out.println("options size=" + qd.options.size());
    qd.options.forEach(o -> System.out.println("  option: id=" + o.id + ", originOrder=" + o.originOrder + ", describe=" + o.describe + ", correct=" + o.correct));
    System.out.println("answersObject size=" + qd.answersObject.size());
    qd.answersObject.forEach(a -> System.out.println("  answerObj: id=" + a.id + ", answer=" + a.answer + ", bankId=" + a.bankId));
  }
}

