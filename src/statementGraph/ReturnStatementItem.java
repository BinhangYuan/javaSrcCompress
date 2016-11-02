package statementGraph;

import org.eclipse.jdt.core.dom.ReturnStatement;


public class ReturnStatementItem extends ElementItem{

	private ReturnStatement astNode; 
	
	
	public ReturnStatementItem(ReturnStatement astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
		this.setLineCount(astNode.toString());
	}
	
	public ReturnStatement getASTNode(){
		return this.astNode;
	}

	@Override
	protected void setLineCount(String code) {
		super.lineCount = code.split(System.getProperty("line.separator")).length;
	}
	
	@Override
	protected void printName() {
		System.out.print("Return Statement: "+astNode.toString());
	}

	@Override
	protected void printDebug() {
		System.out.print("Return Statement: "+astNode.toString());
		System.out.println("Successor: -->");
		if(super.getSeqSuccessor() == null){
			System.out.println("null");
		}
		else{
			super.getSeqSuccessor().printName();
		}
	}
}

