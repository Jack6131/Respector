package org.rest.Respector.AppMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rest.Respector.EndPointRecog.PreprocessFramework;

import soot.*;
import soot.Scene;
import soot.options.Options;

public class Main {
  public static void main(String[] args) {
    assert args.length>=2;
    List<String> argsList=Arrays.asList(args);
    List<String> process_dir=argsList.subList(0, args.length-1);
    
    String outputFile=argsList.get(args.length-1);

    String sourceDirectory = System.getProperty("user.dir");

    // reset Soot state
    G.reset();

    Options.v().set_prepend_classpath(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_keep_line_number(true);
    Options.v().set_exclude(List.of("jdk.*"));

    Options.v().set_process_dir(process_dir);

    // set Soot to retrieve class info from the source API
    Options.v().set_soot_classpath(sourceDirectory);

    Options.v().set_omit_excepting_unit_edges(true);

    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("jb", "preserve-source-annotations:true");

    Options.v().set_write_local_annotations(true);

    Options.v().set_whole_program(true);
    // Call-graph options
    Options.v().setPhaseOption("cg", "library:any-subtype");
    Options.v().setPhaseOption("cg", "all-reachable");

    // Disable CHA call-graph construction
    Options.v().setPhaseOption("cg.cha","enabled:false");

    // Enable SPARK call-graph construction
    Options.v().setPhaseOption("cg.spark","enabled:true");

    // Enable BDD call-graph construction
    // Options.v().setPhaseOption("cg.paddle", "enabled");

    // ensure no methods, packages, or classes are excluded
    Options.v().set_no_bodies_for_excluded(true);
    Scene.v().loadNecessaryClasses();

    //Get endpoint info for the current API
    PreprocessFramework endPointInfoWithData=PreprocessFramework.getEndPointInfo(Scene.v());
    
    Options.v().set_output_format(Options.output_format_jimple);

    Transform MyApp1=new Transform("wjtp.MyApp", new MainTransform(endPointInfoWithData, outputFile));
    PackManager.v().getPack("wjtp").add(MyApp1);

    PackManager.v().runPacks();
  }
}
