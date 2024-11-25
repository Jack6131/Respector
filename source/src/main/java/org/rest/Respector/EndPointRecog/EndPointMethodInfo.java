package org.rest.Respector.EndPointRecog;

import soot.SootMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointMethodInfo {
  public transient SootMethod method;
  public String name;
  public ArrayList<String> requestMethod;
  public ArrayList<EndPointParamInfo> parameterInfo;
  public ArrayList<String> methodMappingPath;
  public ArrayList<String> classMappingPath;
  public ArrayList<EndPointParamInfo> fieldParameterInfo;
  public HashMap<EndPointParamInfo, String> fieldParameterRegex;
  final public int responseStatus;
  public transient final boolean hasPathExplosion;
  public transient ArrayList<EndPointMethodInfo> parentResourceMethod=new ArrayList<>();

  protected transient ArrayList<Pair<String, ArrayList<EndPointParamInfo>>> allPaths;
  protected transient ArrayList<Triple<String, ArrayList<EndPointParamInfo>, String>> allPathPathParamOpTriple;

  protected static final Pattern pathParamPattern = Pattern.compile("\\{(\\w+)\\}");
  protected static final Pattern pathParamPatternWithRegex = Pattern.compile("\\{(\\w+:.+)\\}");

  private static Logger logger = LoggerFactory.getLogger(EndPointMethodInfo.class);

  public EndPointMethodInfo(SootMethod method, String name, ArrayList<String> requestMethod, ArrayList<String> methodMappingPath,
  ArrayList<String> classMappingPath, ArrayList<EndPointParamInfo> fieldParameterInfo, int responseStatus, boolean hasPathExplosion) {
    this.method = method;
    this.name = name;
    this.requestMethod = requestMethod;
    this.parameterInfo = new ArrayList<>();
    this.methodMappingPath = methodMappingPath;
    this.classMappingPath = classMappingPath;
    this.fieldParameterInfo=fieldParameterInfo;
    this.fieldParameterRegex=new HashMap<>();
    this.responseStatus=responseStatus;
    this.hasPathExplosion=hasPathExplosion;
  }

  public EndPointMethodInfo(SootMethod method, String name, ArrayList<String> requestMethod,
      ArrayList<EndPointParamInfo> parameterInfo, ArrayList<String> methodMappingPath, ArrayList<String> classMappingPath, ArrayList<EndPointParamInfo> fieldParameterInfo,
      int responseStatus, boolean hasPathExplosion) {
    this.method = method;
    this.name = name;
    this.requestMethod = requestMethod;
    this.parameterInfo = parameterInfo;
    this.methodMappingPath = methodMappingPath;
    this.classMappingPath = classMappingPath;
    this.fieldParameterInfo=fieldParameterInfo;
    this.fieldParameterRegex=new HashMap<>();
    this.responseStatus=responseStatus;
    this.hasPathExplosion=hasPathExplosion;
  }

  public List<EndPointParamInfo> getPathParams() {
    return this.parameterInfo.stream().filter(pI-> pI.in==paramLoction.path).collect(Collectors.toList());
  }
  
  /**
   * 
   */
  public ArrayList<Pair<String, ArrayList<EndPointParamInfo>>> getMappingPathAndParentPathParams() {
    if(this.allPaths!=null){
      return this.allPaths;
    }

    // create tree map mapping each part of the path to parameter info
    TreeSet<Pair<String, ArrayList<EndPointParamInfo>>> cps=new TreeSet<>();
    for(String classMapping: this.classMappingPath){
      cps.add(Pair.of(classMapping, new ArrayList<>()));
    }

    // if this endpoint has at least one parent method:
    if(!parentResourceMethod.isEmpty()){
      // for each parent do the following:
      for(EndPointMethodInfo parentResourceEP: parentResourceMethod){
        // recursive call - get the mapping and parameter info for the parent
        ArrayList<Pair<String, ArrayList<EndPointParamInfo>>> parentsMapping = parentResourceEP.getMappingPathAndParentPathParams();
        // get the parameter info for the parent of the current endpoint
        List<EndPointParamInfo> parentPathParams = parentResourceEP.getPathParams();


        // if there are no path parameters in the parent resource endpoint, add
        // entire parent path 
        if(parentPathParams.isEmpty()){
          cps.addAll(parentsMapping);
        }
        else{
          for(Pair<String, ArrayList<EndPointParamInfo>> pM: parentsMapping){
            // create arraylist of path parameters for the parent path
            ArrayList<EndPointParamInfo> pathParms=new ArrayList<>(pM.getRight());

            // add parent path parameters to arraylist
            pathParms.addAll(parentPathParams);

            // add the parent class mapping and path parameters 
            cps.add(Pair.of(pM.getLeft(), pathParms));
          }
        }
        
      }
    }

    // add empty path info to tree set
    if(cps.isEmpty()){
      cps.add(Pair.of("", new ArrayList<>()));
    }

    // create method mapping path arraylist
    ArrayList<String> mps=new ArrayList<>(methodMappingPath);
    if(mps.isEmpty()){
      mps.add("");
    }

    ArrayList<Pair<String, ArrayList<EndPointParamInfo>>> mappingPaths=new ArrayList<>();

    for(Pair<String, ArrayList<EndPointParamInfo>> cp: cps){
      for(String mp: mps){

        /// TODO: use regex replace
        String path=String.format("%s/%s",cp.getLeft(),mp).replaceAll("//", "/").replaceAll("//", "/");
        if(!path.equals("/") && path.charAt(path.length()-1)=='/'){
          // get entire path minus the final slash
          path=path.substring(0, path.length()-1);
        }

        ArrayList<EndPointParamInfo> fieldPathParams=new ArrayList<>();

        // create matcher to detect occurences of path parameters in the path
        Matcher m1=pathParamPattern.matcher(path);

        while (m1.find()) {
          // retrieve the first parameter occurrence
          String pName=m1.group(1);

          if(! this.parameterInfo.stream().anyMatch(ep-> ep.name.equals(pName))
          && ! cp.getRight().stream().anyMatch(ep -> ep.name.equals(pName))){
            Optional<EndPointParamInfo> p1=this.fieldParameterInfo.stream().filter(ep->ep.name.equals(pName)).findFirst();

            if(p1.isPresent()){
              fieldPathParams.add(p1.get());
              logger.debug(String.format("Found path parameter %s of %s", pName, path));
            }
            else{
              fieldPathParams.add(new EndPointParamInfo(pName, 0, true,  paramLoction.path, null, null));
              logger.debug(String.format("Failed to locate path parameter %s of %s", pName, path));
            }
          }
          
        }

        Matcher m2=pathParamPatternWithRegex.matcher(path);
        StringBuilder sb = new StringBuilder();

        while (m2.find()) {
          String[] pSecs=m2.group(1).split(":", 2);

          assert pSecs.length==2;

          String pName=pSecs[0];
          String pReg=pSecs[1];

          m2.appendReplacement(sb, String.format("{%s}", pName));

          if(! this.parameterInfo.stream().anyMatch(ep-> ep.name.equals(pName))
          && ! cp.getRight().stream().anyMatch(ep -> ep.name.equals(pName))){
            Optional<EndPointParamInfo> p1=this.fieldParameterInfo.stream().filter(ep->ep.name.equals(pName)).findFirst();

            if(p1.isPresent()){
              fieldPathParams.add(p1.get());
              logger.debug(String.format("Found path parameter %s of %s", pName, path));

              this.fieldParameterRegex.put(p1.get(), pReg);

            }
            else{
              EndPointParamInfo t1=new EndPointParamInfo(pName, 0, true,  paramLoction.path, null, null);
              fieldPathParams.add(t1);

              logger.debug(String.format("Failed to locate path parameter %s of %s", pName, path));

              this.fieldParameterRegex.put(t1, pReg);
            }
          }
        }

        m2.appendTail(sb);

        String cleanPath=sb.toString();

        ArrayList<EndPointParamInfo> requiredPathParams=new ArrayList<>(cp.getRight());
        requiredPathParams.addAll(fieldPathParams);

        mappingPaths.add(Pair.of(cleanPath, requiredPathParams));
      }
    }

    this.allPaths=mappingPaths;

    return mappingPaths;
  }

  /**
   * this method returns an arraylist of triples containing the names of various paths connected
   * to the current endpoint. TODO
   */
  public ArrayList<Triple<String, ArrayList<EndPointParamInfo>, String>> getPathAndParentPathParamAndOpTuple(){
    if(this.allPathPathParamOpTriple!=null){
      return this.allPathPathParamOpTriple;
    }

    if(this.requestMethod.isEmpty()){
      this.allPathPathParamOpTriple=new ArrayList<>();
      return this.allPathPathParamOpTriple;
    }

    // get mappings to the current endpoint and the parent TODO
    ArrayList<Pair<String, ArrayList<EndPointParamInfo>>> mappings=this.getMappingPathAndParentPathParams();

    ArrayList<Triple<String, ArrayList<EndPointParamInfo>, String>> allPathsBound=new ArrayList<>();

    for(Pair<String, ArrayList<EndPointParamInfo>> pathPathParam: mappings){    
      // if(path.equals("/contributors")){
      //   logger.debug(path);
      // }

      for(String rm: this.requestMethod){
        allPathsBound.add(Triple.of(pathPathParam.getLeft(), pathPathParam.getRight(),rm));
      }
    }
    
    this.allPathPathParamOpTriple=allPathsBound;
    
    return allPathsBound;
  }
}
