package org.rest.Respector.archive.GExprTranslate;

import org.rest.Respector.PathCondExtract.ConditionPred;
import org.rest.Respector.PathCondExtract.EndPointParameter;

import soot.ArrayType;
import soot.SootFieldRef;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;

public class SchemaMinItems {
  public int minItems;
  public EndPointParameter epp;

  public SchemaMinItems(int minItems, EndPointParameter epp) {
    this.minItems = minItems;
    this.epp = epp;
  }

  public static SchemaMinItems toMinItems(ConditionPred pred) {
    if(!(pred.evalTo instanceof Boolean && pred._pred instanceof ConditionExpr)){
      return null;
    }

    Value op1=pred.uniCond.getOp1();
    Value op2=pred.uniCond.getOp2();
    if(pred.uniCond instanceof GeExpr){

      if(op1 instanceof InstanceFieldRef && op2 instanceof IntConstant){
        InstanceFieldRef lhs=(InstanceFieldRef)op1;
        IntConstant rhs=(IntConstant) op2;
        
        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootFieldRef ref= lhs.getFieldRef();
          if(epp.getType() instanceof ArrayType && ref.name().equals("length")){
            return new SchemaMinItems(rhs.value, epp);
          }
        }
      }
    }
    else if(pred.uniCond instanceof GtExpr){

      if(op1 instanceof InstanceFieldRef && op2 instanceof IntConstant){
        InstanceFieldRef lhs=(InstanceFieldRef)op1;
        IntConstant rhs=(IntConstant) op2;
        
        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootFieldRef ref= lhs.getFieldRef();
          if(epp.getType() instanceof ArrayType && ref.name().equals("length")){
            return new SchemaMinItems(rhs.value+1, epp);
          }
        }
      }
    }
    else if(pred.uniCond instanceof EqExpr){

      if(op1 instanceof InstanceFieldRef && op2 instanceof IntConstant){
        InstanceFieldRef lhs=(InstanceFieldRef)op1;
        IntConstant rhs=(IntConstant) op2;
        
        if(lhs.getBase() instanceof EndPointParameter){
          EndPointParameter epp=(EndPointParameter) lhs.getBase();

          SootFieldRef ref= lhs.getFieldRef();
          if(epp.getType() instanceof ArrayType && ref.name().equals("length")){
            return new SchemaMinItems(rhs.value, epp);
          }
        }
      }
    }

    return null;
  }
}
