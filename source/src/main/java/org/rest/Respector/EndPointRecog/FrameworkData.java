package org.rest.Respector.EndPointRecog;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;

public class FrameworkData {
  // name of framework of API whose specifications are being generated
  public enum FrameworkName {
    @SerializedName("Unknown")
    Unknown,

    @SerializedName("Spring")
    Spring,

    @SerializedName("JAX")
    JAX,
  }

  public FrameworkName name;
  public String fullName;
  // package names used in this framework, compared against to determine framework of an API
  public List<String> packageNames; 
  public ArrayList<String> packagePrefix;
  
  // framework-specific class/method annotations as described on page 4 of the
  // "Generating REST API Specifications through Static Analysis" paper
  public Map<String,ClassMethodAnnotation> classAnnotations;
  public Map<String,ClassMethodAnnotation> methodAnnotations;

  public Map<String,ClassMethodAnnotation> responseStatusAnnotations;

  // framework-specific parameter annotations as described on page 4-5 of the
  // "Generating REST API Specifications through Static Analysis" paper
  @SerializedName("parameterAnnotations")
  public Map<String,ParameterAnnotation> paramAnnotations;

  // framework-specific method field annotations
  @SerializedName("methodFieldAnnotations")
  public Map<String,ParameterAnnotation> fieldAnnotations;

  public ArrayList<String> responseClasses;

  public ArrayList<StaticResponseInfo> enumResponses;
  public ArrayList<ResponseBuilderInfo> responseBuilders;

  public Integer nullReturnCode;

  public transient TreeMap<Integer, StaticResponseInfo> statusCodeToResponse;
  public transient TreeMap<String, StaticResponseInfo> nameToResponse;

  public void buildResponseMap() {
    this.statusCodeToResponse=new TreeMap<>();
    this.nameToResponse=new TreeMap<>();

    for(StaticResponseInfo info: this.enumResponses){      
      statusCodeToResponse.put(info.statusCode, info);
      nameToResponse.put(info.name, info);
    }
  }

  // data specific to each framework type, such as framework-specific class annotations,
  // loaded from database stored in JSON
  public static final Map<FrameworkName, FrameworkData> Data=loadJSON("FrameworkData.json");
  
  /** this function loads JSON info from the relevant json file to be stored in the class.
   *  this function is used in Respector to load the database info containing framework-specific
   *  patterns and annotations for each API framework.
   * 
   * @param path: path to the JSON file containing API framework info.
   */
  public static Map<FrameworkName, FrameworkData> loadJSON(String path){
    InputStream is= FrameworkData.class.getClassLoader().getResourceAsStream(path);
    Map<FrameworkName, FrameworkData> rtv;

    Gson gson = new Gson();
    JsonReader reader;
    try {
      reader= new JsonReader(new InputStreamReader(is));
      rtv = gson.fromJson(reader, new TypeToken<Map<FrameworkName, FrameworkData>>(){}.getType());

    } catch (Exception e) {
      throw new RuntimeException("Unable to load framework data.");
    }

    for(FrameworkData frameworkData: rtv.values()){
      frameworkData.buildResponseMap();
    }

    return rtv;
  }
}
