package com.github.xionghui.microservice.business.bean.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.bean.enums.ILanguageEnum;

public enum ProjectTypeEnum implements ILanguageEnum {
  Inside("inside", "business-p00000"), // 内部
  Outside("outside", "business-p00001"), // 外部
  ;

  public final String code;
  public final String languageCode;

  private ProjectTypeEnum(String code, String languageCode) {
    this.code = code;
    this.languageCode = languageCode;
  }

  private static final JSONArray VALUE_ARRAY = new JSONArray();
  private static final Set<String> VALUE_SET = new HashSet<>();
  private static final JSONObject VALUE_JSON = new JSONObject();
  private static final Map<String, JSONObject> VALUE_MAP = new HashMap<>();

  static {
    for (ProjectTypeEnum theEnum : ProjectTypeEnum.values()) {
      JSONObject value = new JSONObject();
      VALUE_ARRAY.add(value);
      value.put("code", theEnum.code);
      value.put(CommonConstants.LANGUAGE_CODE_ENUM, theEnum.getLanguageCode());

      VALUE_SET.add(theEnum.code);

      VALUE_JSON.put(theEnum.code, value);

      VALUE_MAP.put(theEnum.code, value);
    }
  }

  public static JSONArray getArray() {
    JSONArray copy = new JSONArray();
    for (Object obj : VALUE_ARRAY) {
      JSONObject json = (JSONObject) obj;
      copy.add(json.clone());
    }
    return copy;
  }

  public static JSONObject getValue(String code) {
    JSONObject copy = VALUE_MAP.get(code);
    return copy == null ? new JSONObject() : (JSONObject) copy.clone();
  }

  public static boolean checkValue(String requireType) {
    return VALUE_SET.contains(requireType);
  }

  public static JSONObject getValues() {
    JSONObject copy = new JSONObject();
    for (Map.Entry<String, Object> entry : VALUE_JSON.entrySet()) {
      JSONObject json = (JSONObject) entry.getValue();
      copy.put(entry.getKey(), json.clone());
    }
    return copy;
  }

  @Override
  public String getLanguageCode() {
    return this.languageCode;
  }
}
