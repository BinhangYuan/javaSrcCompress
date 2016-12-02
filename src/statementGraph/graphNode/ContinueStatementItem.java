package statementGraph.graphNode;
import org.eclipse.jdt.core.dom.ContinueStatement;

public class ContinueStatementItem extends ElementItem{

	private ContinueStatement astNode; 
	
	public ContinueStatementItem(ContinueStatement astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
	}
	
	public ContinueStatement getASTNode(){
		return this.astNode;
	}
	
	@Override
	public void printName() {
		System.out.print("Break Statement: "+astNode.toString());
	}

	@Override
	public void printDebug() {
		System.out.print("Break Statement: "+astNode.toString());
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
