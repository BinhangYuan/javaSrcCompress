package statementGraph;

import org.eclipse.jdt.core.dom.IfStatement;

public class IfStatementItem extends ElementItem{

	private IfStatement astNode; 
	
	public IfStatementItem(IfStatement astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
		this.setLineCount(astNode.toString());
	}
	
	public IfStatement getASTNode(){
		return this.astNode;
	}
	
	@Override
	protected void setLineCount(String code) {
		//It should be the length excluding the body.
		int total = code.split(System.getProperty("line.separator")).length;
		int thenblock = (astNode.getThenStatement()==null ? 0 : astNode.getThenStatement().toString().split(System.getProperty("line.separator")).length);
		int elseblock = (astNode.getElseStatement()==null ? 0 : astNode.getElseStatement().toString().split(System.getProperty("line.separator")).length);
		super.lineCount = total - thenblock - elseblock; //Maybe problematic, check again! 
	}

	@Override
	protected void print() {
		System.out.print("If Statement: "+astNode.toString());
	}

}

