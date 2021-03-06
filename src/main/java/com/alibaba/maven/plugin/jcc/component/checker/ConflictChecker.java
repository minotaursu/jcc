package com.alibaba.maven.plugin.jcc.component.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.maven.plugin.jcc.component.printer.ConflictPrinter;
import com.alibaba.maven.plugin.jcc.pojo.ConflictResult;
import com.alibaba.maven.plugin.jcc.pojo.Header;
import com.alibaba.maven.plugin.jcc.pojo.JccArtifact;
import com.alibaba.maven.plugin.jcc.rule.ConflictRule;
import com.alibaba.maven.plugin.jcc.util.ArtifactUtil;

public class ConflictChecker extends AbstractChecker<ConflictRule>  {
	
	//private ConflictRule conflictRule;	
	private Collection<JccArtifact> jarDependencyTree;
	private Collection<JccArtifact> projectDependencyTree;
	
	public ConflictChecker(ConflictRule conflictRule){		
		super(conflictRule);			
		jarDependencyTree = conflictRule.getCheckArtifactsMap().values();
		projectDependencyTree = conflictRule.getProjectArtifactsMap().values();		
		this.headers = construnctHeader();
		this.printer = new ConflictPrinter(this);
	}	
	
	@Override
	public List<Header> construnctHeader(){
		List<Header> headers = new ArrayList<Header>();
		
		Header header1 = new Header();
		header1.setName(ArtifactUtil.mavenProject2Str(rule.getCheckProject()));
		header1.setDesc("P");
		header1.setJarSize(jarDependencyTree.size());
		headers.add(header1);
		
		Header header2 = new Header();
		header2.setName(ArtifactUtil.mavenProject2Str(rule.getProject()));
		header2.setJarSize(projectDependencyTree.size());
		headers.add(header2);
		return headers;
	}	

	@Override
	public void transformConflictResult() throws Exception {
		
		List<JccArtifact> jarDependencyClass =  ArtifactUtil.getAllClassByArtifactsAsList(jarDependencyTree,"param");		
		Map<String,List<JccArtifact>> projectDependencyClass = ArtifactUtil.getAllClassByArtifactsAsMap(projectDependencyTree);
		
		conflictResults =  transformConflictResult(jarDependencyClass,projectDependencyClass);
	}		
	
	
	public  List<ConflictResult> transformConflictResult(List<JccArtifact> jarDependencyClass,Map<String,List<JccArtifact>> projectDependencyClass){
		if((jarDependencyClass == null || jarDependencyClass.size() == 0) || 
				(projectDependencyClass == null || projectDependencyClass.isEmpty())){
			System.out.println("no jar need to be check conflict");
			return null;
		}		
		
	    List<ConflictResult>  conflictResults = new ArrayList<ConflictResult>();
		
		for(JccArtifact jar : jarDependencyClass){		
			List<String> classes = jar.getClasses();
			if(classes == null || classes.size() == 0){	
				System.out.println("jar ["+jar.getName()+"] has no class");
				continue;
			}
			ConflictResult conflictResult = new ConflictResult();
			conflictResult.setConflictParamJar(jar);
			for(String clazz : classes){
				if(!projectDependencyClass.keySet().contains(clazz)){
					continue;
				}		
				conflictResult.addConflictClass(clazz);
				List<JccArtifact> jars = projectDependencyClass.get(clazz);				
				conflictResult.addConflictProjectJars(jars);			
			}
			if(conflictResult.getConflictProjectJars() != null && conflictResult.getConflictProjectJars().size() > 0){
				conflictResults.add(conflictResult);
			}			
		}
		
		return conflictResults;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub		
	}

	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}		

}
