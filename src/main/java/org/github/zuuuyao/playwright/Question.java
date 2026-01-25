package org.github.zuuuyao.playwright;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Question {
  public String id;
  public String describe;
  public String category;
  public String difficultyName;
  public String createTime;
  public String auditStatus;
  public boolean examed;
  public boolean boutique;
  public boolean examable;
  public boolean exercise;
  public String attribute;
  public String bankId;

  public List<Option> options = new ArrayList<>();
  public List<AnswerObj> answers = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public Question(Map<?, ?> m) {
    if (m == null) return;
    Object o1 = m.get("id");
    this.id = o1 == null ? null : o1.toString();
    Object od = m.get("describe");
    this.describe = od == null ? null : od.toString();
    Object cat = m.get("category");
    this.category = cat == null ? null : cat.toString();
    Object diff = m.get("difficultyName");
    this.difficultyName = diff == null ? null : diff.toString();
    Object ct = m.get("createTime");
    this.createTime = ct == null ? null : ct.toString();
    Object a = m.get("auditStatus");
    this.auditStatus = a == null ? null : a.toString();

    Object examedObj = m.get("examed");
    this.examed = examedObj instanceof Boolean ? (Boolean) examedObj : false;
    Object boutiqueObj = m.get("boutique");
    this.boutique = boutiqueObj instanceof Boolean ? (Boolean) boutiqueObj : false;
    Object examableObj = m.get("examable");
    this.examable = examableObj instanceof Boolean ? (Boolean) examableObj : false;
    Object exerciseObj = m.get("exercise");
    this.exercise = exerciseObj instanceof Boolean ? (Boolean) exerciseObj : false;

    Object attr = m.get("attribute");
    this.attribute = attr == null ? null : attr.toString();
    Object bid = m.get("bankId");
    this.bankId = bid == null ? null : bid.toString();

    Object opts = m.get("options");
    if (opts instanceof List) {
      for (Object o : (List<?>) opts) {
        if (o instanceof Map) options.add(new Option((Map<?, ?>) o));
      }
    }

    Object ans = m.get("answers");
    if (ans instanceof List) {
      for (Object aobj : (List<?>) ans) {
        if (aobj instanceof Map) answers.add(new AnswerObj((Map<?, ?>) aobj));
      }
    }

    // 兼容 answersObject 或 answersObject 字段
    Object ans2 = m.get("answersObject");
    if (ans2 instanceof List) {
      for (Object aobj : (List<?>) ans2) {
        if (aobj instanceof Map) answers.add(new AnswerObj((Map<?, ?>) aobj));
      }
    }
  }

  public static class Option {
    public boolean correct;
    public int answerSeq;
    public String originOrder;
    public String describe;
    public String id;
    public int orders;

    public Option(Map<?, ?> m) {
      if (m == null) return;
      Object c = m.get("correct");
      this.correct = c instanceof Boolean ? (Boolean) c : false;
      Object seq = m.get("answerSeq");
      if (seq instanceof Number) this.answerSeq = ((Number) seq).intValue();
      else if (seq != null) {
        try { this.answerSeq = Integer.parseInt(seq.toString()); } catch (Exception ignored) {}
      }
      Object oo = m.get("originOrder");
      this.originOrder = oo == null ? null : oo.toString();
      Object d = m.get("describe");
      this.describe = d == null ? null : d.toString();
      Object idObj = m.get("id");
      this.id = idObj == null ? null : idObj.toString();
      Object ord = m.get("orders");
      if (ord instanceof Number) this.orders = ((Number) ord).intValue();
      else if (ord != null) {
        try { this.orders = Integer.parseInt(ord.toString()); } catch (Exception ignored) {}
      }
    }
  }

  public static class AnswerObj {
    public String id;
    public Integer answer;
    public Integer orders;
    public String bankId;

    public AnswerObj(Map<?, ?> m) {
      if (m == null) return;
      Object idObj = m.get("id");
      this.id = idObj == null ? null : idObj.toString();
      Object ans = m.get("answer");
      if (ans instanceof Number) this.answer = ((Number) ans).intValue();
      else if (ans != null) {
        try { this.answer = Integer.parseInt(ans.toString()); } catch (Exception ignored) {}
      }
      Object ord = m.get("orders");
      if (ord instanceof Number) this.orders = ((Number) ord).intValue();
      Object bid = m.get("bankId");
      this.bankId = bid == null ? null : bid.toString();
    }
  }

  @Override
  public String toString() {
    return "Question{id=" + id + ", describe=" + (describe == null ? "" : (describe.length() > 80 ? describe.substring(0, 80) + "..." : describe)) +
           ", options=" + options.size() + ", answers=" + answers.size() + "}";
  }
}

