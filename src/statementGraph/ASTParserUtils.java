package statementGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import ilpSolver.NaiveBinaryIPSolver;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV1;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV2;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV3;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV4;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV5;
import statementGraph.constraintAndFeatureEncoder.ConstraintAndFeatureEncoderV6;
import statementGraph.graphNode.StatementWrapper;

public class ASTParserUtils {
	static boolean debug = false;
	//use ASTParse to parse string
	public static void parse(String filePath, String fileName) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		//parser.setS
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
		
		cu.accept(new ASTVisitor() {
			
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(debug){
					System.out.println("Vistor:: Method Declaration of: '"+name+ "' at line" +cu.getLineNumber(node.getStartPosition()));
				}
				methods.add(node);
				return true;
			}
		});
		if(debug){
			System.out.println("All Methods");
		}
		for(MethodDeclaration node: methods){
			if(debug){
				System.out.println("Method Declaration of: '"+node.getName()+ "' at line" +cu.getLineNumber(node.getStartPosition()));
				System.out.println(node.toString());
				//System.out.println("Generate CFG:");
			}
			
			SimplifiedAST sAST = new SimplifiedAST(node);
			CFG cfg = new CFG(sAST);
			//cfg.printCFG();
			//System.out.println("Generate DDG:");
			DDG ddg = new DDG(sAST);
			//ddg.printDDG();
			ConstraintAndFeatureEncoderV1 encoder = new ConstraintAndFeatureEncoderV1(sAST,cfg,ddg);
			//encoder.printConstraints();
			NaiveBinaryIPSolver solver = new NaiveBinaryIPSolver();
			solver.setDependenceConstraints(encoder.getASTConstraints(), encoder.getCFGConstraints(), encoder.getDDGConstraints());
			solver.setLineCostConstraints(encoder.getLineCounts());
			int targetLine = 5;
			solver.setTargetLineCount(targetLine);
			boolean[] solution = solver.solve();
			if(debug){
				System.out.println(sAST.computeOutput(solution));
			}
		}
	}

	
	//This is used for a simple verification 
	public static void parseVaraibleName(String filePath, String fileName) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<SimpleName> varaibles = new ArrayList<SimpleName>();
			
		cu.accept(new ASTVisitor() {
				
			public boolean visit(SimpleName node){
				varaibles.add(node);
				return true;
			}
		});
		if(debug){	
			System.out.println("All varabile declaration:");
			for(SimpleName node: varaibles){
			
				if(node.resolveBinding().getKind()==IBinding.VARIABLE && node.isDeclaration()){
					System.out.println("SimpleName: " + node.getIdentifier());
					System.out.println("is variable and defined at line" +cu.getLineNumber(node.getStartPosition()));
				}
				else if(node.resolveBinding().getKind()==IBinding.VARIABLE && !node.isDeclaration()){
					System.out.println("SimpleName: " + node.getIdentifier());
					System.out.println("is variable and used at line" +cu.getLineNumber(node.getStartPosition()));
				}
				else{
					System.out.println("SimpleName: " + node.getIdentifier());
					System.out.println("is not variable!");
				}
			}
		}
	}
	
	
	//read file content into a string
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
	 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			//System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
	 
		reader.close();
	 
		return  fileData.toString();	
	}
	
	
	//loop directory to get file list (Not used yet!)
	public static void ParseFilesInDir() throws IOException{
		File dirs = new File(".");
		String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator;
	 
		File root = new File(dirPath);
		//System.out.println(rootDir.listFiles());
		File[] files = root.listFiles ( );
	 
		for (File f : files ) {
			String filePath = f.getAbsolutePath();
			if(f.isFile()){
				System.out.println("Handle file:"+filePath);
				//parse(readFileToString(filePath));
			}
		}
	}
	
	
	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV1 parseMethodV1(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
				
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
				
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
				
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		ConstraintAndFeatureEncoderV1 encoder = new ConstraintAndFeatureEncoderV1(sAST,cfg,ddg);
					
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				System.out.println("========>");
			}
			
			if(manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
			
		return encoder;
	}
	
	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV2 parseMethodV2(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
					
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
					
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
					
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		ConstraintAndFeatureEncoderV2 encoder = new ConstraintAndFeatureEncoderV2(sAST,cfg,ddg);
						
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				System.out.println("========>");
			}
				
			if(manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
				
		return encoder;
	}
	
	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV3 parseMethodV3(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
						
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
						
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
						
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		ConstraintAndFeatureEncoderV3 encoder = new ConstraintAndFeatureEncoderV3(sAST,cfg,ddg);
							
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				//System.out.println("Parent type:"+StatementWrapper.parentStatementTypeInt2String(item.getParentType()));
				System.out.println("========>");
			}
					
			if(manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
					
		return encoder;
	}	
	
	
	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV4 parseMethodV4(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
							
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
							
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
							
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line " +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		if(print){
			ddg.printDDG();
		}
		ConstraintAndFeatureEncoderV4 encoder = new ConstraintAndFeatureEncoderV4(sAST,cfg,ddg);
								
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				//System.out.println("Parent type:"+StatementWrapper.parentStatementTypeInt2String(item.getParentType()));
				System.out.println("Nested Level: "+item.getNestedLevel());
				System.out.println("========>");
			}
						
			if(manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
						
		return encoder;
	}	

	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV5 parseMethodV5(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
							
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
							
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
							
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line " +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		if(print){
			ddg.printDDG();
		}
		ConstraintAndFeatureEncoderV5 encoder = new ConstraintAndFeatureEncoderV5(sAST,cfg,ddg);
								
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				//System.out.println("Parent type:"+StatementWrapper.parentStatementTypeInt2String(item.getParentType()));
				//System.out.println("Nested Level: "+item.getNestedLevel());
				System.out.println("Variable Count: "+item.getReferencedVariables());
				System.out.println("========>");
			}
						
			if(manualLabel!=null && manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
						
		return encoder;
	}
	
	//use ASTParse to parse string
	public static ConstraintAndFeatureEncoderV6 parseMethodV6(boolean print, String filePath, String fileName, String methodName, int pos, boolean [] manualLabel) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String str = readFileToString(filePath+fileName);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[]{filePath}, null, null, true);
		parser.setUnitName("Anything");
		parser.setResolveBindings(true);
								
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
								
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				SimpleName name = node.getName();
				if(name.getIdentifier().equals(methodName) && pos == cu.getLineNumber(node.getStartPosition())){
					methods.add(node);
				}
				return true;
			}
		});
								
		Assert.isTrue(methods.size()==1);
		MethodDeclaration method = methods.get(0);
		
		if(print){
			System.out.println("Method Declaration of: '"+method.getName()+ "' at line " +cu.getLineNumber(method.getStartPosition()));
			//System.out.println("Method Declaration of: '"+method.getName()+ "' at line" +cu.getLineNumber(method.getStartPosition()));
			System.out.println(method.toString());
		}
		SimplifiedAST sAST = new SimplifiedAST(method);
		CFG cfg = new CFG(sAST);
		DDG ddg = new DDG(sAST);
		if(print){
			ddg.printDDG();
		}
		ConstraintAndFeatureEncoderV6 encoder = new ConstraintAndFeatureEncoderV6(sAST,cfg,ddg);
									
		if(print){
			List<StatementWrapper> statements = sAST.getAllWrapperList();
			System.out.println("Statements:");
			for(int i=0 ; i < statements.size(); i++){
				StatementWrapper item = statements.get(i);
				System.out.println("Node "+i+": <========");
				System.out.println(item.toString());
				System.out.println("Variable Count: "+item.getReferencedVariables());
				System.out.println("========>");
			}
							
			if(manualLabel!=null && manualLabel.length == statements.size()){
				System.out.println("Manual labeled result:");
				System.out.println(sAST.computeOutput(manualLabel));
				System.out.println("Reduced line count: "+sAST.computeBodyLines(manualLabel));
			}
			//encoder.printConstraints();
		}
							
		return encoder;
	}
}