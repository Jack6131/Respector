package org.rest.Respector.EndPointRecog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Iterator;


import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.options.Options;
import soot.plugins.SootPhasePlugin;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;
import soot.Type;
import soot.dava.internal.AST.ASTTryNode.container;
import soot.Local;
import soot.Body;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.*;
import soot.jimple.internal.JIfStmt;
import soot.Printer;
import soot.RefType;
import soot.BriefUnitPrinter;

import org.apache.commons.lang3.tuple.Pair;
import org.rest.Respector.EndPointRecog.FrameworkData.FrameworkName;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessFramework {

  public transient FrameworkData frameworkData;
  public ArrayList<EndPointMethodInfo> endPointMethodData;

  private static Logger logger = LoggerFactory.getLogger(PreprocessFramework.class);
    
  public PreprocessFramework(FrameworkData frameworkData, ArrayList<EndPointMethodInfo> endPointMethodData) {
    this.frameworkData = frameworkData;
    this.endPointMethodData = endPointMethodData;
  }

  /** This method decides whether the current API uses the framework Jersey or Spring Boot based on its included
   * classes.
   * 
   * @param libraryClasses: library classes included by the API.
   * @param phantomClasses: phantom classes within the API.
   */
  public static FrameworkName decideFramework(Chain<SootClass> libraryClasses, Chain<SootClass> phantomClasses) {    
    // add both types of classes into a single stream containing all classes
    Stream<SootClass> allClasses= Stream.concat(phantomClasses.stream(), libraryClasses.stream());
    Iterator<SootClass> iter = allClasses.iterator();

    // iterate over all classes in the stream. for each class, check package against database packages.
    while(iter.hasNext()){
      // get package name in the next class
      SootClass c=iter.next();
      String p=c.getPackageName();

      // compare package name against database package name
      for(FrameworkData d:FrameworkData.Data.values()){
        // go through package names for the current potential API framework
        for(String s:d.packageNames){
          // if there is a match with any specific framework, return the framework of the current API
          if(p.equals(s)){
            logger.info("The REST API uses "+d.name);
            return d.name;
          }
        }
      }
    }

    return FrameworkName.Unknown;
  }

  
  public static PreprocessFramework getEndPointInfo(Scene v){
    return getEndPointInfo(v, null, null, null);
  }

  /** This method gets the endpoint info associated with a scene containing API data for an API
   *  of one of two framework types (Jersey or Spring Boot)
   * 
   * @param v: scene containing the class and method info for the API
   * @param excludedMethodName: methods excluded from analysis
   * @param excludedPackageName: package excluded from analysis
   * @param excludedPathName: class excluded from analysis
   */
  public static PreprocessFramework getEndPointInfo(Scene v, String excludedMethodName, String excludedPackageName, String excludedClassName) {
    // initialize classes used in API
    Chain<SootClass> appClasses=v.getApplicationClasses();
    Chain<SootClass> libClasses=v.getLibraryClasses();
    Chain<SootClass> phantomClasses=v.getPhantomClasses();
    
    // determine REST API framework used
    FrameworkName framework=decideFramework(libClasses, phantomClasses);

    if(framework==FrameworkName.Unknown){
      throw new RuntimeException("Unknown Framework");
    }
    
    // initialize empty array list of endpoint method info
    ArrayList<EndPointMethodInfo> rtv=new ArrayList<>();

    // get framework data from the database of framework patterns (as mentioned in page 4 of
    // the "Generating REST API Specifications through Static Analysis" paper )
    FrameworkData data=FrameworkData.Data.get(framework);

    // initialize empty hashmap to store endpoint method info specifically associated with each
    // class
    HashMap<SootClass, ArrayList<EndPointMethodInfo>> resrcClassToMethods=new HashMap<>();

    // add all application classes in the API to a list and sort them in descending
    // order based on class name
    ArrayList<Pair<SootClass, String>> sortedAppClasses=new ArrayList<>();
    for(SootClass c: appClasses){
      sortedAppClasses.add(Pair.of(c, c.getName()));
    }
    sortedAppClasses.sort((b,a)->a.getRight().compareTo(b.getRight()));

    // for each class, C:
    for(Pair<SootClass, String> p: sortedAppClasses){
      SootClass c=p.getLeft(); // get the class info associated with the pair

      // if there is an excluded package or class, skip to next iteration of the loop
      if(excludedPackageName!=null && !excludedPackageName.equals(c.getPackageName())){
        continue;
      }

      if(excludedClassName!=null && !excludedClassName.equals(c.getShortName())){
        continue;
      }

      VisibilityAnnotationTag cTags= (VisibilityAnnotationTag) c.getTag("VisibilityAnnotationTag");

      ArrayList<String> classPath=new ArrayList<>();
      ArrayList<EndPointParamInfo> fieldPathParams=new ArrayList<>();

      if(cTags != null){
        ArrayList<AnnotationTag> classAnnos = cTags.getAnnotations();
        for(AnnotationTag t: classAnnos){
          ClassMethodAnnotation CMAnno = data.classAnnotations.get(t.getType());

          if(CMAnno != null){
            classPath.addAll(CMAnno.getPathFrom(t));
          }
        }
      }

      if(!data.fieldAnnotations.isEmpty()){
        for(SootField f: c.getFields()){
          VisibilityAnnotationTag fieldTag=(VisibilityAnnotationTag) f.getTag("VisibilityAnnotationTag");
          
          if(fieldTag==null){
            continue;
          }
  
          ArrayList<VisibilityAnnotationTag> fieldAnnos=new ArrayList<>(List.of(fieldTag));
          List<Type> fieldType=List.of(f.getType());
          
          ArrayList<EndPointParamInfo> fieldInfo=getAnnotatedParam(fieldAnnos, data.fieldAnnotations, fieldType);

          if(!fieldInfo.isEmpty()){
            fieldPathParams.add(fieldInfo.get(0));

            logger.debug(String.format("Found annotated field %s in %s", f.getName(), c.getName()));
          }
        }
      }

      // for each method M in class C, do the following:
      for(SootMethod m:c.getMethods()){

        if(excludedMethodName!=null && !excludedMethodName.equals(m.getName())){
          continue;
        }

        VisibilityAnnotationTag tags = (VisibilityAnnotationTag) m.getTag("VisibilityAnnotationTag");
        if(tags==null){

          continue;
        }

        ArrayList<String> requestMethod=new ArrayList<>();
        ArrayList<String> methodPath=new ArrayList<>();
        int responseStatus=200;

        boolean hasMethodAnnotationsNoSkip=false;

        boolean hasPathExplosion=false;

        for(AnnotationTag mTag: tags.getAnnotations()){
          String annoType = mTag.getType();

          if(
            annoType.equals("Lorg/respector/SkipEndPointForPathExplosion;")
            ){
            hasPathExplosion=true;
            continue;
          }

          ClassMethodAnnotation CMAnno = data.methodAnnotations.get(annoType);

          if(CMAnno != null){
            hasMethodAnnotationsNoSkip=true;
            
            methodPath.addAll(CMAnno.getPathFrom(mTag));

            requestMethod.addAll(CMAnno.getRequestMethodFrom(mTag));
          }

          // get response type and status codes
          ClassMethodAnnotation CRAnno = data.responseStatusAnnotations.get(annoType);
          if(CRAnno!=null){
            Integer rtStatus=CRAnno.getResponseStatus(mTag, data.nameToResponse);
            if(rtStatus!=null){
              responseStatus=rtStatus;
            }
          }
        }

        if(!hasMethodAnnotationsNoSkip){
          continue;
        }

        ///TODO: fix this
        if(requestMethod.isEmpty()){
          if(framework==FrameworkName.Spring){
            requestMethod.addAll(List.of("get", "post", "head", "options", "put", "patch", "delete", "trace"));
          }
        }

        // store tags to check if fields are annotated (see getAnnotatedParam function)
        VisibilityParameterAnnotationTag paramTags=(VisibilityParameterAnnotationTag) m.getTag("VisibilityParameterAnnotationTag");
        ArrayList<EndPointParamInfo> paramInfo;
        if(paramTags==null){
          // initialize empty array list of info about each method parameter
          paramInfo=new ArrayList<>();
        }
        else{
          // get annotations associated with parameters
          ArrayList<VisibilityAnnotationTag> paramAnnos=paramTags.getVisibilityAnnotations();
          // get parameter types to later retrieve parameters
          List<Type> paramTypes= m.getParameterTypes();
          
          // get info about each method parameter
          paramInfo=getAnnotatedParam(paramAnnos, data.paramAnnotations, paramTypes);

          // DONE: no argument end point?
          // keep them
          // if(paramInfo.isEmpty()){
            // continue;
          // }
        }
        EndPointMethodInfo EPInfo=new EndPointMethodInfo(m, m.getName(), requestMethod, paramInfo, methodPath, classPath, fieldPathParams, responseStatus, hasPathExplosion);
        rtv.add(EPInfo);

        ArrayList<EndPointMethodInfo> epms = resrcClassToMethods.computeIfAbsent(c, x-> new ArrayList<>());
        epms.add(EPInfo);
      }

    }

    linkSubResources(resrcClassToMethods);

    return new PreprocessFramework(data, rtv);
  }

  public static void linkSubResources(HashMap<SootClass, ArrayList<EndPointMethodInfo>> resrcClassToMethods) {
    for(Map.Entry<SootClass, ArrayList<EndPointMethodInfo>> kv: resrcClassToMethods.entrySet()){
      SootClass c=kv.getKey();
      ArrayList<EndPointMethodInfo> endPoints=kv.getValue();
      for(EndPointMethodInfo ep: endPoints){
        SootMethod m=ep.method;

        Type rType = m.getReturnType();

        if(rType instanceof RefType){
          RefType refRtv=(RefType) rType;

          SootClass clz=refRtv.getSootClass();

          if(clz.equals(c)){
            logger.error(String.format("method %s returns its own class %s", m.getSignature(), c.getName()));
            continue;
          }

          if(resrcClassToMethods.containsKey(clz)){
            for(EndPointMethodInfo subEP: resrcClassToMethods.get(clz)){
              subEP.parentResourceMethod.add(ep);
            }
          }
        }
      }
    }
  }

  public static boolean endpointAnnoCheck(AnnotationTag tag, Map<String,ClassMethodAnnotation> methodAnnotations){   
    return methodAnnotations.containsKey(tag.getType());
  }
  

  public static ArrayList<EndPointParamInfo> getAnnotatedParam(ArrayList<VisibilityAnnotationTag> tagList, Map<String,ParameterAnnotation> paramAnnotations, List<Type> paramTypes) {
    ArrayList<EndPointParamInfo> rtv=new ArrayList<>();
    
    int len=tagList.size();
    for(int i=0;i<len;++i){
      // check if the field is annotated 
      VisibilityAnnotationTag tag=tagList.get(i);

      if(tag==null){
        continue;
      }

      String name=null;
      Boolean required=null;
      paramLoction in=null;
      String defaultValue=null;

      boolean hasParamAnnotation=false;

      for(AnnotationTag pTag: tag.getAnnotations()){
        String type1=pTag.getType();
        ParameterAnnotation PAnno = paramAnnotations.get(type1);

        if(PAnno != null){
          hasParamAnnotation=true;

          if(name==null){
            name=PAnno.getNameFrom(pTag);
          }

          if(required==null){
            required=PAnno.getRequiredFrom(pTag);
          }

          if(in==null){
            in=PAnno.getInFrom(pTag);
          }

          if(defaultValue==null){
            defaultValue=PAnno.getDefaultValueFrom(pTag);
          }

        }
      }

      if(!hasParamAnnotation){
        continue;
      }

      if(paramLoction.path.equals(in)){
        required=true;
      }
      if(name==null){
        name="";
      }
      
      rtv.add(new EndPointParamInfo(name, i, required, in, defaultValue, paramTypes.get(i)));
      
    }

    return rtv;
  }
  
}
