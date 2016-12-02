package statementGraph.graphNode;

import org.eclipse.jdt.core.dom.ExpressionStatement;

public class ExpressionStatementItem extends ElementItem{

	private ExpressionStatement astNode; 
	
	
	public ExpressionStatementItem(ExpressionStatement astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
	}
	
	public ExpressionStatement getASTNode(){
		return this.astNode;
	}
	
	@Override
	public void printName() {
		System.out.print("Expression Statement: "+astNode.toString());
	}

	@Override
	public void printDebug() {
		System.out.print("Expression Statement: "+astNode.toString());	
		System.out.println("Successor: -->");
		if(super.getCFGSeqSuccessor() == null){
			System.out.println("null");
		}
		else{
			super.getCFGSeqSuccessor().printName();
		}
		super.printDDGPredecessor();
	}
	
	@Override
	public String toString() {
		return astNode.toString();
	}

	@Override
	public int getLineCount() {
		return astNode.toString().split(System.getProperty("line.separator")).length;
	}
}

