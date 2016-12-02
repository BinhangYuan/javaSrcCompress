package statementGraph.graphNode;

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

//[ Expression . ] super( [ Expression { , Expression } ] ) ;
public class SuperConstructorInvocationStatementItem extends ElementItem{

	private SuperConstructorInvocation astNode; 
	
	
	public SuperConstructorInvocationStatementItem(SuperConstructorInvocation astNode){
		this.astNode = astNode;
		super.setType(astNode.getNodeType());
	}
	
	public SuperConstructorInvocation getASTNode(){
		return this.astNode;
	}

	@Override
	public void printName() {
		System.out.print("Super Constructor Invocation Statement: "+astNode.toString());
	}
	
	@Override
	public void printDebug() {
		System.out.print("Super Constructor Invocation Statement: "+astNode.toString());
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
