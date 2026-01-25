package org.github.zuuuyao.playwright;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiResult {
  public int status;
  public boolean ok;
  public Body body;
  public String rawBodyString; // 如果 body 不是对象，保存原始文本

  @SuppressWarnings("unchecked")
  public ApiResult(Map<?, ?> map) {
    if (map == null) return;
    Object s = map.get("status");
    if (s instanceof Number) this.status = ((Number) s).intValue();
    else if (s != null) {
      try { this.status = Integer.parseInt(s.toString()); } catch (Exception ignored) {}
    }
    Object o = map.get("ok");
    if (o instanceof Boolean) this.ok = (Boolean) o;

    Object b = map.get("body");
    if (b instanceof Map) {
      this.body = new Body((Map<?, ?>) b);
    } else if (b instanceof String) {
      this.rawBodyString = (String) b;
    }
  }

  public static class Body {
    public List<Question> list = new ArrayList<>();
    public int total;
    public int qsNum; // sometimes named qsNum/tsNum/qsNum

    @SuppressWarnings("unchecked")
    public Body(Map<?, ?> m) {
      if (m == null) return;
      Object t = m.get("total");
      if (t instanceof Number) this.total = ((Number) t).intValue();
      else if (t != null) {
        try { this.total = Integer.parseInt(t.toString()); } catch (Exception ignored) {}
      }
      Object qs = m.get("qsNum");
      if (qs == null) qs = m.get("tsNum"); // try alternative name
      if (qs instanceof Number) this.qsNum = ((Number) qs).intValue();
      else if (qs != null) {
        try { this.qsNum = Integer.parseInt(qs.toString()); } catch (Exception ignored) {}
      }

      Object listObj = m.get("list");
      if (listObj instanceof List) {
        for (Object o : (List<?>) listObj) {
          if (o instanceof Map) {
            this.list.add(new Question((Map<?, ?>) o));
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    if (body != null) {
      return "ApiResult{status=" + status + ", ok=" + ok + ", listSize=" + body.list.size() + ", total=" + body.total + ", qsNum=" + body.qsNum + "}";
    }
    return "ApiResult{status=" + status + ", ok=" + ok + ", rawBody=" + rawBodyString + "}";
  }
}

