package org.github.zuuuyao.playwright;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionDetail {
  public String id;
  public String type;
  public String typeName;
  public String describe;
  public List<String> category = new ArrayList<>();
  public String analysis;
  public String answers; // e.g. "D"
  public List<Option> options = new ArrayList<>();
  public String difficulty;
  public String difficultyName;
  public List<String> label = new ArrayList<>();
  public String createTime;
  public boolean examed;
  public boolean boutique;
  public boolean examable;
  public boolean exercise;
  public boolean languages;
  public String attribute;
  public Object cognitionLevel;
  public Map<?, ?> answerKey;
  public List<AnswerObj> answersObject = new ArrayList<>();
  public List<Object> qsCategoriesName = new ArrayList<>();
  public String bankId;
  public String questionBankId;
  public String useType;
  public Double difficultyNumber;
  public String mark;
  public Object cateids;
  public Integer isFeedback;
  public Integer handledErrorCount;
  public Integer allErrorCount;
  public List<Object> subQuestion = new ArrayList<>();
  public List<Object> answerPoints = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public QuestionDetail(Map<?, ?> m) {
    if (m == null) return;
    Object o = m.get("id"); this.id = o == null ? null : o.toString();
    Object t = m.get("type"); this.type = t == null ? null : t.toString();
    Object tn = m.get("typeName"); this.typeName = tn == null ? null : tn.toString();
    Object d = m.get("describe"); this.describe = d == null ? null : d.toString();

    Object cat = m.get("category");
    if (cat instanceof List) {
      for (Object c : (List<?>) cat) { if (c != null) category.add(c.toString()); }
    } else if (cat != null) {
      category.add(cat.toString());
    }

    Object a = m.get("analysis"); this.analysis = a == null ? null : a.toString();
    Object ans = m.get("answers"); this.answers = ans == null ? null : ans.toString();

    Object opts = m.get("options");
    if (opts instanceof List) {
      for (Object oobj : (List<?>) opts) {
        if (oobj instanceof Map) options.add(new Option((Map<?, ?>) oobj));
      }
    }

    Object diff = m.get("difficulty"); this.difficulty = diff == null ? null : diff.toString();
    Object diffN = m.get("difficultyName"); this.difficultyName = diffN == null ? null : diffN.toString();

    Object lbl = m.get("label"); if (lbl instanceof List) { for (Object l : (List<?>) lbl) if (l!=null) label.add(l.toString()); }
    Object ct = m.get("createTime"); this.createTime = ct == null ? null : ct.toString();

    Object examedObj = m.get("examed"); this.examed = examedObj instanceof Boolean ? (Boolean) examedObj : false;
    Object boutiqueObj = m.get("boutique"); this.boutique = boutiqueObj instanceof Boolean ? (Boolean) boutiqueObj : false;
    Object examableObj = m.get("examable"); this.examable = examableObj instanceof Boolean ? (Boolean) examableObj : false;
    Object exerciseObj = m.get("exercise"); this.exercise = exerciseObj instanceof Boolean ? (Boolean) exerciseObj : false;
    Object langObj = m.get("languages"); this.languages = langObj instanceof Boolean ? (Boolean) langObj : false;

    Object attr = m.get("attribute"); this.attribute = attr == null ? null : attr.toString();
    this.cognitionLevel = m.get("cognitionLevel");

    Object ak = m.get("answerKey"); if (ak instanceof Map) this.answerKey = (Map<?, ?>) ak;

    Object ansObj = m.get("answersObject");
    if (ansObj instanceof List) {
      for (Object ao : (List<?>) ansObj) {
        if (ao instanceof Map) answersObject.add(new AnswerObj((Map<?, ?>) ao));
      }
    }

    Object qsNames = m.get("qsCategoriesName"); if (qsNames instanceof List) this.qsCategoriesName.addAll((List<?>) qsNames);

    Object bid = m.get("bankId"); this.bankId = bid == null ? null : bid.toString();
    Object qbid = m.get("questionBankId"); this.questionBankId = qbid == null ? null : qbid.toString();
    Object ut = m.get("useType"); this.useType = ut == null ? null : ut.toString();

    Object dn = m.get("difficultyNumber"); if (dn instanceof Number) this.difficultyNumber = ((Number) dn).doubleValue();
    else if (dn != null) { try { this.difficultyNumber = Double.parseDouble(dn.toString()); } catch (Exception ignored) {} }

    Object mk = m.get("mark"); this.mark = mk == null ? null : mk.toString();
    this.cateids = m.get("cateids");

    Object ifb = m.get("isFeedback"); if (ifb instanceof Number) this.isFeedback = ((Number) ifb).intValue();
    Object hec = m.get("handledErrorCount"); if (hec instanceof Number) this.handledErrorCount = ((Number) hec).intValue();
    Object aec = m.get("allErrorCount"); if (aec instanceof Number) this.allErrorCount = ((Number) aec).intValue();

    Object subs = m.get("subQuestion"); if (subs instanceof List) this.subQuestion.addAll((List<?>) subs);
    Object ap = m.get("answerPoints"); if (ap instanceof List) this.answerPoints.addAll((List<?>) ap);
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
      Object c = m.get("correct"); this.correct = c instanceof Boolean ? (Boolean) c : false;
      Object seq = m.get("answerSeq"); if (seq instanceof Number) this.answerSeq = ((Number) seq).intValue(); else if (seq!=null) { try { this.answerSeq = Integer.parseInt(seq.toString()); } catch (Exception ignored) {} }
      Object oo = m.get("originOrder"); this.originOrder = oo == null ? null : oo.toString();
      Object d = m.get("describe"); this.describe = d == null ? null : d.toString();
      Object idObj = m.get("id"); this.id = idObj == null ? null : idObj.toString();
      Object ord = m.get("orders"); if (ord instanceof Number) this.orders = ((Number) ord).intValue(); else if (ord!=null) { try { this.orders = Integer.parseInt(ord.toString()); } catch (Exception ignored) {} }
    }
  }

  public static class AnswerObj {
    public String id;
    public Integer answer;
    public Integer orders;
    public String bankId;

    public AnswerObj(Map<?, ?> m) {
      if (m == null) return;
      Object idObj = m.get("id"); this.id = idObj == null ? null : idObj.toString();
      Object ans = m.get("answer"); if (ans instanceof Number) this.answer = ((Number) ans).intValue(); else if (ans!=null) { try { this.answer = Integer.parseInt(ans.toString()); } catch (Exception ignored) {} }
      Object ord = m.get("orders"); if (ord instanceof Number) this.orders = ((Number) ord).intValue();
      Object bid = m.get("bankId"); this.bankId = bid == null ? null : bid.toString();
    }
  }

  @Override
  public String toString() {
    return "QuestionDetail{id=" + id + ", describe=" + (describe == null ? "" : (describe.length()>80?describe.substring(0,80)+"...":describe)) + ", options=" + options.size() + ", answersObject=" + answersObject.size() + "}";
  }
}

