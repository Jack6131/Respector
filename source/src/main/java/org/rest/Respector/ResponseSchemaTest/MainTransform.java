package org.rest.Respector.ResponseSchemaTest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.z3.BoolExpr;

import soot.ArrayType;
import soot.Body;
import soot.BriefUnitPrinter;
import soot.G;
import soot.Hierarchy;
import soot.Local;
import soot.RefType;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Scene;
import soot.Value;
import soot.ValueBox;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JThrowStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.Chain;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.rest.Respector.EndPointRecog.EndPointMethodInfo;
import org.rest.Respector.EndPointRecog.EndPointParamInfo;
import org.rest.Respector.EndPointRecog.PreprocessFramework;
import org.rest.Respector.EndPointRecog.StaticResponseInfo;
import org.rest.Respector.EndPointRecog.ParameterAnnotation.paramLoction;
import org.rest.Respector.GenericRequestParam.ExtractParamFromReq;
import org.rest.Respector.Interdependence.GlobalVarInterdependency;
import org.rest.Respector.LoopInfo.MethodLoopInfo;
import org.rest.Respector.MyPassBase.MyTransformBase;
import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;
import org.rest.Respector.PathCondExtract.EndpointAnalysis;
import org.rest.Respector.PathCondExtract.PathConstraint;
import org.rest.Respector.PathRecord.MethodEntryRecord;
import org.rest.Respector.PathRecord.ParamInfo;
import org.rest.Respector.PathRecord.PathRecord;
import org.rest.Respector.PathRecord.PathRecordBase;
import org.rest.Respector.PathRecord.PathRecordBase.RecordType;
import org.rest.Respector.Simplification.ClusterSimpl;
import org.rest.Respector.Simplification.DisjunctOverConjuncSimpl;
import org.rest.Respector.Simplification.PathFeasibility;
import org.rest.Respector.Simplification.SimplificationResult;
import org.rest.Respector.Simplification.SootToZ3;
import org.rest.Respector.SpecGen.SpecGen;
import org.rest.Respector.SpecGen.Spec.ReferenceObj;
import org.rest.Respector.SpecGen.Spec.SpecObj;
import org.rest.Respector.SpecGen.Spec.Components.GlobalVarInfo;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.XPathItemObjSerializer;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalRefs.GlobalRefObj;
import org.rest.Respector.SpecGen.Spec.Components.EndPointConstraints.GlobalWrites.GlobalWriteObj;
import org.rest.Respector.SpecGen.Spec.Components.InterDependence.GlobalInterDepItemObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObj;
import org.rest.Respector.SpecGen.Spec.Path.PathItemObjSerializer;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.EndPointOperationObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.ParameterObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Examples.ExampleObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.ParamSchemaObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Parameters.Schema.Items.ParamSchemaItemsObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses.ResponseObj;
import org.rest.Respector.SpecGen.Spec.Path.EndPointOp.Responses.ResponseSchemaGen;
import org.rest.Respector.StaticVarAssignment.StaticExampleLoc;
import org.rest.Respector.StaticVarAssignment.StaticVarAssignment;
import org.rest.Respector.Z3ToOAS.CgToGlobalKeyword;
import org.rest.Respector.Z3ToOAS.ConstraintToExamples;
import org.rest.Respector.Z3ToOAS.ToParamKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainTransform extends MyTransformBase {
  public MainTransform(PreprocessFramework preprocessReuslt, String outputFilePath) {
    super(preprocessReuslt, false);

    this.outputFile=Paths.get(outputFilePath);
  }

  // protected int globalUIDCnt = 0;
  protected ArrayList<SootField> UIDToGlobal = new ArrayList<>();

  protected HashMap<SootField, GlobalVarInfo> globalMap = new HashMap<>();

  private static Logger logger = LoggerFactory.getLogger(MainTransform.class);

  public Path outputFile;

  protected int operationIdCnt=0;
  String getNewOperationId(){
    return String.valueOf(this.operationIdCnt++);
  }

  static void updataEmptyEndPointParamName(ArrayList<EndPointParamInfo> paramInfo, Map<Integer, String> passParamName, Map<Integer, ParameterObj> paramSMap){
    for(EndPointParamInfo pI: paramInfo){

      String namePass=passParamName.get(pI.index);

      if(pI.name.isEmpty() && !namePass.isEmpty()){
        pI.name=namePass;

        logger.warn("updated name for EPP %d: %s", pI.index, pI.name);

        paramSMap.get(pI.index).name=namePass;
      }
    }
  }

  static TreeMap<Integer, ParameterObj> createParams(ArrayList<EndPointParamInfo> paramInfo, EndPointOperationObj endPointOperationObj, EndPointMethodInfo EPInfo){
    TreeMap<Integer, ParameterObj> paramSMap=new TreeMap<>();
    for(EndPointParamInfo pI: paramInfo){
      if(pI.in==paramLoction.formData || pI.in==paramLoction.body){
        endPointOperationObj.createRequestBodyParamObj(pI);
        continue;
      }

      ParameterObj parameterObj= endPointOperationObj.createParameterObj(pI);
      ParamSchemaObj schemaObj=parameterObj.createSchema(pI.type);
      schemaObj.setDefault(pI.defaultValue);
      paramSMap.put(pI.index, parameterObj);
    }

    logger.info("total number of endpoint parameters for this method detected: " + paramInfo.size());

    ArrayList<String> reqParams=ExtractParamFromReq.extractParamFromRequest(Scene.v().getCallGraph(), EPInfo);
    for(String paramName: reqParams){
      endPointOperationObj.createRequestBodyParamObj(paramName, "string");
    }

    if(!reqParams.isEmpty()){
      logger.info(String.format("%d endpoint parameters in request body", reqParams.size()));
    }

    return paramSMap;
  }

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {

    icfg = new JimpleBasedInterproceduralCFG();
    printerSet = new HashMap<Body, BriefUnitPrinter>();
    // hierarchy = Scene.v().getActiveHierarchy();
    bodyToLoopInfoCache = new HashMap<>();

    buildCHACallGraph();

    // int sum=0;
    boolean printRaw = false;
    // boolean translateKeyword = true;

    SpecObj specObj = new SpecObj();

    int nEP=this.preprocessReuslt.endPointMethodData.size();
    logger.info("total # of endpoint methods detected: " + nEP);

    HashMap<GlobalVarInfo, ArrayList<EndPointMethodInfo>> globalToWriters=new HashMap<>();
    HashMap<GlobalVarInfo, ArrayList<EndPointMethodInfo>> globalToReaders=new HashMap<>();

    HashMap<EndPointMethodInfo, ArrayList<Triple<String, String, String>>> endpointToPathsToCopies=new HashMap<>();

    HashSet<Type> returnTypes=new HashSet<>();

    for (int iEP=0;iEP<nEP;++iEP) {
      EndPointMethodInfo EPInfo =this.preprocessReuslt.endPointMethodData.get(iEP);
      SootMethod m = EPInfo.method;
      String operationId=getNewOperationId();
      ArrayList<EndPointParamInfo> paramInfo = EPInfo.parameterInfo;

      // if(!m.getName().equals("deleteApi")){
      //   continue;
      // }
      // if(iEP>10){
      //   continue;
      // }
      logger.info(String.format("%d/%d analyzing endpoint method %s", iEP, nEP, m.getSignature()));

      if(!m.hasActiveBody()){
        logger.warn(String.format("endpoint %s has no active body", m.getSignature()));
        continue;
      }
      
      ArrayList<Triple<String, ArrayList<EndPointParamInfo>, String>> allPathsBound=EPInfo.getPathAndParentPathParamAndOpTuple();

      if(allPathsBound.isEmpty()){
        logger.warn(String.format("endpoint %s has no HTTP method", m.getSignature()));

        continue;
      }
      
      Type rt=m.getReturnType();
      returnTypes.add(rt);

      if(true)continue;

      EndPointOperationObj endPointOperationObj=null;
      int firstBind=0;
      int nPaths=allPathsBound.size();
      for(firstBind=0; firstBind<nPaths ; ++firstBind){
        Triple<String, ArrayList<EndPointParamInfo>, String> trip = allPathsBound.get(firstBind);
        Pair<String, String> bind0= Pair.of(trip.getLeft(),trip.getRight()) ;
        endPointOperationObj=specObj.createEndPointOperation(bind0.getLeft(), bind0.getRight(), operationId);

        if(endPointOperationObj!=null){
          endpointToPathsToCopies.computeIfAbsent(EPInfo, e-> new ArrayList<>()).add(Triple.of(operationId, bind0.getLeft(), bind0.getRight()));

          break;
        }
      }

      if(firstBind==nPaths 
        || endPointOperationObj==null //subsumed by the first condition
      ){
        continue;
      }

      TreeMap<Integer, ParameterObj> paramSMap=createParams(paramInfo, endPointOperationObj, EPInfo);
    
      
      // should we build paths of no endpoint param?
      if (
        true
        // ||
        // !EPInfo.hasPathExplosion 
        // && 
        // !paramInfo.isEmpty()
        ) {
        EndpointAnalysis pass = new EndpointAnalysis(EPInfo, this, 5000);
       
        HashSet<Integer> validStatusCode = new HashSet<>();
        HashSet<Integer> invalidStatusCode = new HashSet<>();


        SimplificationResult S_tmp=null;

        while (true) {
          boolean hasNextChunk= pass.buildPaths();

          ArrayList<PathConstraint> validPaths=pass.getValidPathsAndClear();

          for (PathConstraint p : validPaths) {
            validStatusCode.add(p.HTTPStatusCode);
  
            if (printRaw) {
              ArrayList<String> condStr = new ArrayList<>();
              for (ConditionPred pred : p.conds) {
                if (pred.inLoopHeader) {
                  continue;
                }
                condStr.add(String.format("%s", pred.uniCond.toString()));
              }
  
              if (!condStr.isEmpty()) {
                endPointOperationObj.addRawValid(condStr);
              }
            }
          }

          SimplificationResult s1 = SimplificationResult.doSimplification(validPaths);
          
          if(S_tmp==null){
            S_tmp=s1;
          }
          else{
            S_tmp.merge(s1);
          }

          ArrayList<PathConstraint> invalidPaths = pass.getInvalidPathsAndClear();

          for (PathConstraint p : invalidPaths) {
            invalidStatusCode.add(p.HTTPStatusCode);
  
            if (printRaw) {
              ArrayList<String> condStr = new ArrayList<>();
              for (ConditionPred pred : p.conds) {
                if (pred.inLoopHeader) {
                  continue;
                }
  
                condStr.add(String.format("%s", pred.uniCond));
              }
  
              if (!condStr.isEmpty()) {
                endPointOperationObj.addRawInvalid(condStr);
              }
            }
  
          }

          if(!hasNextChunk){
            break;
          }
        }

        updataEmptyEndPointParamName(paramInfo, pass.paramNames, paramSMap);

        TreeMap<Integer, StaticResponseInfo> resMap=this.preprocessReuslt.frameworkData.statusCodeToResponse;
        for (Integer code : validStatusCode) {
          StaticResponseInfo resInfo=resMap.get(code);
          String description=resInfo!=null?resInfo.name:"";
          endPointOperationObj.addResponse(new ResponseObj(code.toString(), description));
        }

        for (Integer code : invalidStatusCode) {
          StaticResponseInfo resInfo=resMap.get(code);
          String description=resInfo!=null?resInfo.name:"";
          endPointOperationObj.addResponse(new ResponseObj(code.toString(), description));
        }

        boolean onlyInvalid = validStatusCode.isEmpty();
        if (onlyInvalid) {// skip the endpoint method that are not supported yet
          continue;
        }

        // endPointOperationObj.addValidCond("See rawValid because you skipped simplification module");
        logger.info(String.format("%d clusters in commonPreds", S_tmp.commonPreds.size()));

        for(HashSet<ConditionPred> predCluster: S_tmp.commonPreds.values()){
          ClusterSimpl cSimpl=new ClusterSimpl(predCluster);
          ArrayList<ConditionPred> output=cSimpl.output;
          ArrayList<String> conjunction=new ArrayList<>();
          for(ConditionPred pred: output){
            conjunction.add(String.format("( %s )", pred.uniCond.toString()));
          }
          endPointOperationObj.addValidCond(String.join(" && ", conjunction));

          cSimpl.closeCtx();
        }

        
        // endPointOperationObj.addInvalidCond("See rawInvalid because you skipped simplification module");
        

        logger.info(String.format("%d EPP has constraints", S_tmp.C_epp.size()));

        for(Map.Entry<EndPointParameter, HashSet<ArrayList<ConditionPred>>> kv:S_tmp.C_epp.entrySet()){
          EndPointParameter epp=kv.getKey();

          if(!epp.isEPP){
            continue;
          }

          HashSet<ArrayList<ConditionPred>> disjunction=kv.getValue();

          ParameterObj parameterObj=paramSMap.get(epp.idx);

          if(parameterObj==null){
            logger.info(String.format("constraints on Request Body Parameter %s", epp.getName()));
            continue;
          }

          HashSet<Object> examples = ConstraintToExamples.extractExamplesFromCepp(disjunction, epp);
          if(!examples.isEmpty()){
            for(Object eg: examples){
              String egStr=eg.toString();
              ExampleObj egObj=new ExampleObj(eg);
        
              if(parameterObj.containsExampleName(egStr)){
                logger.warn(String.format("key %s already in examples", egStr));
        
                parameterObj.addExample(egObj);
              }
              else{
                parameterObj.addExample(egStr, egObj);
              }
            }
          }

          DisjunctOverConjuncSimpl disjunctOverConjuncSimpl=new DisjunctOverConjuncSimpl(disjunction);
          
          ArrayList<BoolExpr> simplifedCepp=disjunctOverConjuncSimpl.simplifiedGoals;

          if(!simplifedCepp.isEmpty()){
            for(BoolExpr expr: simplifedCepp){
              logger.info("\t"+expr.toString());
            }
  
            ArrayList<BoolExpr> notTranslated=ToParamKeyword.translate(simplifedCepp, epp, parameterObj, disjunctOverConjuncSimpl);

            for(BoolExpr expr: notTranslated){
              String exprStr=expr.toString();
              // endPointOperationObj.addValidCond(exprStr);
              parameterObj.schema.addConstraint(exprStr);
            }
          }

          disjunctOverConjuncSimpl.closeCtx();
        }
        
        HashMap<SootField, GlobalRefObj> endPointGlobalRefMap=new HashMap<>();

        Function<SootField, GlobalVarInfo> makeNewGlobalVar= (g -> {
          int uid=UIDToGlobal.size();
          UIDToGlobal.add(g);
          return new GlobalVarInfo(g.getName(), uid, g.getDeclaringClass().getFilePath());
        });

        for (SootField g : pass.globalVarRead) {
          assert endPointGlobalRefMap.containsKey(g)==false;

          GlobalVarInfo gInfo=globalMap.computeIfAbsent(g, makeNewGlobalVar);

          GlobalRefObj gRefObj=new GlobalRefObj(gInfo);
          endPointOperationObj.addGlobalRefObj(gRefObj);

          endPointGlobalRefMap.put(g, gRefObj);

          ArrayList<EndPointMethodInfo> readerList = globalToReaders.computeIfAbsent(gInfo, c -> new ArrayList<>());
          readerList.add(EPInfo);
        }

        logger.info(String.format("%d global var written", pass.gloablVarWrite.size()));
        for(Map.Entry<SootField, ArrayList<Value>> kv: pass.gloablVarWrite.entrySet()){
          SootField g=kv.getKey();

          GlobalVarInfo gInfo=globalMap.computeIfAbsent(g, makeNewGlobalVar);

          GlobalWriteObj gWrObj=new GlobalWriteObj(gInfo);
          endPointOperationObj.addGlobalWriteObj(gWrObj);

          for(Value v: kv.getValue()){
            gWrObj.addValue(v.toString());
          }

          ArrayList<EndPointMethodInfo> writerList = globalToWriters.computeIfAbsent(gInfo, c -> new ArrayList<>());
          writerList.add(EPInfo);
        }

        logger.info(String.format("%d globals has constraints", S_tmp.C_g.size()));

        for(Map.Entry<SootField, HashSet<ArrayList<ConditionPred>>> kv:S_tmp.C_g.entrySet()){
          SootField g=kv.getKey();

          HashSet<ArrayList<ConditionPred>> disjunction=kv.getValue();

          HashSet<Object> examples=ConstraintToExamples.extractExamplesFromCg(disjunction, g);

          if(!examples.isEmpty()){

            GlobalRefObj gRefObj=endPointGlobalRefMap.get(g);
            if(gRefObj==null){
              GlobalVarInfo gInfo=globalMap.computeIfAbsent(g, makeNewGlobalVar);
  
              gRefObj=new GlobalRefObj(gInfo);
              endPointOperationObj.addGlobalRefObj(gRefObj);

              endPointGlobalRefMap.put(g, gRefObj);
            }

            for(Object eg: examples){
              gRefObj.addExample(eg);
            }
          }

          DisjunctOverConjuncSimpl disjunctOverConjuncSimpl=new DisjunctOverConjuncSimpl(disjunction);
          
          ArrayList<BoolExpr> simplifedCg=disjunctOverConjuncSimpl.simplifiedGoals;

          if(!simplifedCg.isEmpty()){
            for(BoolExpr expr: simplifedCg){
              logger.info("\t"+expr.toString());
            }
            
            GlobalRefObj gRefObj=endPointGlobalRefMap.get(g);
            if(gRefObj==null){
              GlobalVarInfo gInfo=globalMap.computeIfAbsent(g, makeNewGlobalVar);
  
              gRefObj=new GlobalRefObj(gInfo);
              endPointOperationObj.addGlobalRefObj(gRefObj);

              endPointGlobalRefMap.put(g, gRefObj);
            }

            // ArrayList<BoolExpr> notTranslated=CgToGlobalKeyword.translate(simplifedCg, g, gRefObj, disjunctOverConjuncSimpl);

            for(BoolExpr expr: simplifedCg){
              gRefObj.addConstraint(expr.toString());
            }
          }

          disjunctOverConjuncSimpl.closeCtx();
        }
        
      }

      for(int i=firstBind+1;i<nPaths;++i){
        Triple<String, ArrayList<EndPointParamInfo>, String> trip =allPathsBound.get(i);
        Pair<String, String> bindI=Pair.of(trip.getLeft(), trip.getRight());

        EndPointOperationObj epI=new EndPointOperationObj(endPointOperationObj);
        epI.operationId=getNewOperationId();

        for(EndPointParamInfo eppI: trip.getMiddle()){
          if(epI.parameters.stream().anyMatch(p -> p.name.equals(eppI.name))){
            continue;
          }

          ParameterObj parameterObjI = epI.createParameterObj(eppI);
          parameterObjI.createSchema(eppI.type);
        }

        specObj.insertEndPointOperationTo(bindI.getLeft(), bindI.getRight(), epI);
        
        ArrayList<Triple<String, String, String>> copyPaths = endpointToPathsToCopies.get(EPInfo);
        assert copyPaths!=null;
        copyPaths.add(Triple.of(epI.operationId, bindI.getLeft(), bindI.getRight()));
      }

      // add parent params to firstBind
      {
        Triple<String, ArrayList<EndPointParamInfo>, String> trip =allPathsBound.get(firstBind);
        
        for(EndPointParamInfo eppI: trip.getMiddle()){
          if(endPointOperationObj.parameters.stream().anyMatch(p -> p.name.equals(eppI.name))){
            continue;
          }

          ParameterObj parameterObjI = endPointOperationObj.createParameterObj(eppI);
          parameterObjI.createSchema(eppI.type);
        }

      }


    }


    // ArrayList<JsonObject> retSchemas=new ArrayList<>();
    JsonObject jsonObject=new JsonObject();
    for(Type rt: returnTypes){
      String name=rt.toString();

      if(this.preprocessReuslt.frameworkData.responseClasses.contains(name)){
        continue;
      }

      jsonObject.add(name, ResponseSchemaGen.toResponseSchema(rt));
    }

    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      Files.writeString(this.outputFile, gson.toJson(jsonObject));
    } catch (IOException e) {
      System.out.println("An error occurred while appending text to file: " + e.getMessage());
    }

    if(true)return;

    StaticVarAssignment SVA = new StaticVarAssignment(this.CHA_CG, globalMap.keySet());

    for(Map.Entry<SootField, GlobalVarInfo> kv: globalMap.entrySet()){
      SootField g=kv.getKey();
      GlobalVarInfo varInfo = kv.getValue();

      ArrayList<StaticExampleLoc> l=SVA.result.get(g);

      if(l!=null){
        varInfo.locs=l;
      }

      specObj.components.globalsWithStaticVarAssign.put(varInfo.id, varInfo);
    }

    GlobalVarInterdependency globalInter=GlobalVarInterdependency.computeGlobalInterdepend(globalToWriters, globalToReaders);

    TreeMap<Integer, GlobalInterDepItemObj> interdependency = specObj.components.interDependencies.interdependency;
    for(Map.Entry<GlobalVarInfo, Pair<ArrayList<EndPointMethodInfo>, ArrayList<EndPointMethodInfo>>> kv: globalInter.interdependency.entrySet()){
      GlobalVarInfo globaVarInfo=kv.getKey();
      ArrayList<EndPointMethodInfo> writers=kv.getValue().getLeft();
      ArrayList<EndPointMethodInfo> readers=kv.getValue().getRight();

      ReferenceObj globalVarInfoRef=globaVarInfo.refToMe;

      HashMap<String, ReferenceObj> writerRefs=new HashMap<>();
      for(EndPointMethodInfo wr: writers){
        ArrayList<Triple<String, String, String>> ops = endpointToPathsToCopies.get(wr);
        assert ops!=null;

        for(Triple<String, String, String> op: ops){
          String escapedPath=op.getMiddle().replace("/", "~1");
          String componentParentPath=String.format("#/paths/%s/%s", escapedPath, op.getRight());

          writerRefs.put(op.getLeft(), new ReferenceObj(componentParentPath));
        }
      }

      HashMap<String, ReferenceObj> readerRefs=new HashMap<>();
      for(EndPointMethodInfo rr: readers){
        ArrayList<Triple<String, String, String>> ops = endpointToPathsToCopies.get(rr);
        assert ops!=null;

        for(Triple<String, String, String> op: ops){
          String escapedPath=op.getMiddle().replace("/", "~1");
          String componentParentPath=String.format("#/paths/%s/%s", escapedPath, op.getRight());

          readerRefs.put(op.getLeft(), new ReferenceObj(componentParentPath));
        }
      }

      interdependency.put(globaVarInfo.id, new GlobalInterDepItemObj(globalVarInfoRef, readerRefs, writerRefs));
    }


    Gson gson=SpecGen.getSpecBuilder();
    try{
      Files.writeString(this.outputFile, gson.toJson(specObj));
    }
    catch(IOException e){
      logger.error(e.toString());
    }
  }
}
