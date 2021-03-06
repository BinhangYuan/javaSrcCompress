package statementGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import statementGraph.graphNode.DoStatementWrapper;
import statementGraph.graphNode.StatementWrapper;
import statementGraph.graphNode.EnhancedForStatementWrapper;
import statementGraph.graphNode.ForStatementWrapper;
import statementGraph.graphNode.IfStatementWrapper;
import statementGraph.graphNode.LabeledStatementWrapper;
import statementGraph.graphNode.ReturnStatementWrapper;
import statementGraph.graphNode.SwitchStatementWrapper;
import statementGraph.graphNode.TryStatementWrapper;
import statementGraph.graphNode.WhileStatementWrapper;

public class CFG {
	
	private MethodDeclaration methodASTNode;
	
	private HashMap<ASTNode,Integer> astMap = new HashMap<ASTNode,Integer>();
	
	private List<StatementWrapper> nodes = new ArrayList<StatementWrapper>();
	//private List<EdgeItem> edges = new ArrayList<EdgeItem>();
	
	
	public CFG(SimplifiedAST sAST){
		this.astMap.clear();
		this.methodASTNode = sAST.getASTNode();
		this.astMap = sAST.getAstMap();
		this.nodes = sAST.getAllWrapperList();
		buildGraphEdges(this.methodASTNode);
	}	
	
	private void buildGraphEdges(ASTNode node){
		int nodeType = node.getNodeType();
		if(nodeType == StatementWrapper.METHOD_DECLARATION){
			if(((MethodDeclaration)node).getBody()!=null){
				Block methodBody = ((MethodDeclaration)node).getBody();
				assert(astMap.get(methodBody.statements().get(0))==0);
				buildGraphEdges((ASTNode)(methodBody));
			}
		}
		else if(nodeType == StatementWrapper.BLOCK){
			Block body = (Block)node;
			if(body.statements().size()==0){
				return;
			}
			buildGraphEdges((ASTNode)body.statements().get(0));
			int i = 0;
			for(; i< body.statements().size()-1;i++){
				StatementWrapper start = nodes.get(astMap.get(body.statements().get(i)));
				StatementWrapper end = nodes.get(astMap.get(body.statements().get(i+1)));
				if(start.getType() != StatementWrapper.RETURN_STATEMENT){
					start.setCFGSeqSuccessor(end);
				}
				buildGraphEdges((ASTNode)body.statements().get(i));
			}
			buildGraphEdges((ASTNode)body.statements().get(i));
		}
		else if(nodeType == StatementWrapper.ASSERT_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.BREAK_STATEMENT){
			//This is a linear solution, should improve if necessary in the future;
			BreakStatement breakNode = (BreakStatement) node; 
			if(breakNode.getLabel()!=null){
				boolean flag = false;
				for(Object o : nodes){
					StatementWrapper statementItem = (StatementWrapper) o;
					if(statementItem.getType() == StatementWrapper.LABELED_STATEMENT){
						LabeledStatementWrapper labelItem = ((LabeledStatementWrapper)(statementItem));
						if(labelItem.getASTNode().getLabel().getIdentifier().equals(breakNode.getLabel().getIdentifier())){
							flag = true;
							StatementWrapper start = nodes.get(astMap.get(node));
							start.setCFGSeqSuccessor(labelItem.getCFGSeqSuccessor());
							break;
						}
					}
				}
				if(!flag){
					System.out.println("This is an error, can not find the label!");
				}
			}
			// No label, link to the loop.
			else{
				//Find the most immediate loop;
				ASTNode ancestor = node.getParent();
				while(!StatementWrapper.isLoopStatement(ancestor) && ancestor.getNodeType()!=StatementWrapper.SWITCH_STATEMENT){
					ancestor = ancestor.getParent();
				}
				StatementWrapper start = nodes.get(astMap.get(node));
				StatementWrapper end = nodes.get(astMap.get(ancestor)).getCFGSeqSuccessor();
				start.setCFGSeqSuccessor(end);
			}
		}
		else if(nodeType == StatementWrapper.CONSTRUCTOR_INVOCATION){
			return;
		}
		else if(nodeType == StatementWrapper.CONTINUE_STATEMENT){
			//This is a linear solution, should improve if necessary in the future;
			ContinueStatement continueNode = (ContinueStatement) node; 
			if(continueNode.getLabel()!=null){
				boolean flag = false;
				for(Object o : nodes){
					StatementWrapper statementItem = (StatementWrapper) o;
					if(statementItem.getType() == StatementWrapper.LABELED_STATEMENT){
						LabeledStatementWrapper labelItem = ((LabeledStatementWrapper)(statementItem));
						if(labelItem.getASTNode().getLabel().getIdentifier().equals(continueNode.getLabel().getIdentifier())){
							flag = true;
							StatementWrapper start = nodes.get(astMap.get(node));
							start.setCFGSeqSuccessor(labelItem);
							break;
						}
					}
				}
				if(!flag){
					System.out.println("This is an error, can not find the label!");
				}
			}
			// No label, link to the loop.
			else{
				//Find the most immediate loop;
				ASTNode ancestor = node.getParent();
				while(!StatementWrapper.isLoopStatement(ancestor)){
					ancestor = ancestor.getParent();
				}
				StatementWrapper start = nodes.get(astMap.get(node));
				StatementWrapper end = nodes.get(astMap.get(ancestor));//Different from break here.
				start.setCFGSeqSuccessor(end);
			}
		}
		else if(nodeType == StatementWrapper.DO_STATEMENT){
			//This may be problematic 
			DoStatement doNode = (DoStatement) node;
			if(doNode.getBody().getNodeType()==StatementWrapper.BLOCK){
				Block loopBlock = (Block)(doNode.getBody());
				if(loopBlock.statements().size()==0){
					return;
				}
				
				ASTNode firstStatement = (ASTNode) loopBlock.statements().get(0);
				DoStatementWrapper doItem = (DoStatementWrapper)nodes.get(astMap.get(node));
				StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
				doItem.setBodyEntry(firstItem);
				
				ASTNode lastStatement = (ASTNode) loopBlock.statements().get(loopBlock.statements().size()-1);
				StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
				lastItem.setCFGSeqSuccessor(doItem);
				//Recursive
				buildGraphEdges(loopBlock);
			}
			else{
				ASTNode bodyStatement = doNode.getBody();
				StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
				DoStatementWrapper doItem = (DoStatementWrapper)nodes.get(astMap.get(node));
				bodyItem.setCFGSeqSuccessor(doItem);
				doItem.setBodyEntry(bodyItem);
				buildGraphEdges(bodyStatement);
			}
		}
		else if(nodeType == StatementWrapper.EMPTY_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.ENHANCED_FOR_STATEMENT){
			//This may be problematic 
			EnhancedForStatement enforNode = (EnhancedForStatement) node;
			if(enforNode.getBody().getNodeType()==StatementWrapper.BLOCK){
				Block loopBlock = (Block)(enforNode.getBody());
				if(loopBlock.statements().size()==0){
					return;
				}
				
				ASTNode firstStatement = (ASTNode) loopBlock.statements().get(0);
				EnhancedForStatementWrapper enforItem = (EnhancedForStatementWrapper)nodes.get(astMap.get(node));
				StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
				enforItem.setBodyEntry(firstItem);
				
				ASTNode lastStatement = (ASTNode) loopBlock.statements().get(loopBlock.statements().size()-1);
				StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
				lastItem.setCFGSeqSuccessor(enforItem);
				//Recursive
				buildGraphEdges(loopBlock);
			}
			else{
				ASTNode bodyStatement = enforNode.getBody();
				StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
				EnhancedForStatementWrapper enforItem = (EnhancedForStatementWrapper)nodes.get(astMap.get(node));
				bodyItem.setCFGSeqSuccessor(enforItem);
				enforItem.setBodyEntry(bodyItem);
				buildGraphEdges(bodyStatement);
			}
		}
		else if(nodeType == StatementWrapper.EXPRESSION_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.FOR_STATEMENT){
			//This may be problematic 
			ForStatement forNode = (ForStatement) node;
			if(forNode.getBody().getNodeType()==StatementWrapper.BLOCK){
				Block loopBlock = (Block)(forNode.getBody());
				if(loopBlock.statements().size()==0){
					return;
				}
				
				ASTNode firstStatement = (ASTNode) loopBlock.statements().get(0);
				ForStatementWrapper forItem = (ForStatementWrapper)nodes.get(astMap.get(node));
				StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
				forItem.setBodyEntry(firstItem);
				
				ASTNode lastStatement = (ASTNode) loopBlock.statements().get(loopBlock.statements().size()-1);
				StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
				lastItem.setCFGSeqSuccessor(forItem);
				//Recursive
				buildGraphEdges(loopBlock);
			}
			else{
				ASTNode bodyStatement = forNode.getBody();
				StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
				ForStatementWrapper forItem = (ForStatementWrapper)nodes.get(astMap.get(node));
				bodyItem.setCFGSeqSuccessor(forItem);
				forItem.setBodyEntry(bodyItem);
				buildGraphEdges(bodyStatement);
			}
		}
		else if(nodeType == StatementWrapper.IF_STATEMENT){
			IfStatement ifnode = (IfStatement)node;
			IfStatementWrapper ifItem = (IfStatementWrapper)nodes.get(astMap.get(ifnode));
			ASTNode thenNode = ifnode.getThenStatement();
			ASTNode elseNode = ifnode.getElseStatement();
			if(thenNode.getNodeType() == StatementWrapper.BLOCK){
				Block thenBlock = (Block)thenNode;
				if(thenBlock.statements().size()==0){
					return;
				}
				ASTNode firstStatement = (ASTNode) thenBlock.statements().get(0);
				ASTNode lastStatement = (ASTNode) thenBlock.statements().get(thenBlock.statements().size()-1);
				StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
				StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
				lastItem.setCFGSeqSuccessor(ifItem.getCFGSeqSuccessor());
				ifItem.setThenEntry(firstItem);
				//Recursive
				buildGraphEdges(thenBlock);
			}
			else{
				ASTNode bodyStatement = (ASTNode)thenNode;
				StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
				bodyItem.setCFGSeqSuccessor(ifItem.getCFGSeqSuccessor());
				ifItem.setThenEntry(bodyItem);
				buildGraphEdges(bodyStatement);
			}
			if(elseNode != null){//Only cares then branch
				if(elseNode.getNodeType() == StatementWrapper.BLOCK){
					Block elseBlock = (Block)elseNode;
					if(elseBlock.statements().size()==0){
						return;
					}
					ASTNode firstStatement = (ASTNode) elseBlock.statements().get(0);
					ASTNode lastStatement = (ASTNode) elseBlock.statements().get(elseBlock.statements().size()-1);
					StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
					StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
					lastItem.setCFGSeqSuccessor(ifItem.getCFGSeqSuccessor());
					ifItem.setElseEntry(firstItem);
					//Recursive
					buildGraphEdges(elseBlock);
				}
				else{
					ASTNode bodyStatement = (ASTNode)elseNode;
					StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
					bodyItem.setCFGSeqSuccessor(ifItem.getCFGSeqSuccessor());
					ifItem.setElseEntry(bodyItem);
					buildGraphEdges(bodyStatement);
				}
			}
		}
		else if(nodeType == StatementWrapper.LABELED_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.RETURN_STATEMENT){
			ReturnStatementWrapper returnItem = (ReturnStatementWrapper) nodes.get(astMap.get(node));
			if(returnItem.getCFGSeqSuccessor()!=null){
				returnItem.setCFGSeqSuccessor(null);
			}
		}
		else if(nodeType == StatementWrapper.SUPER_CONSTRUCTOR_INVOCATION){
			return;
		}
		else if(nodeType == StatementWrapper.SWITCH_CASE){
			return;//This may be problematic;
		}
		else if(nodeType == StatementWrapper.SWITCH_STATEMENT){
			@SuppressWarnings("unchecked")
			List<Statement> branchNodes = ((SwitchStatement)node).statements();
			SwitchStatementWrapper switchItem =  (SwitchStatementWrapper) nodes.get(astMap.get(node));
			int i = 0;
			for(; i<branchNodes.size()-1 ;i++){
				Statement branchNode = branchNodes.get(i);
				if(branchNode.getNodeType()==StatementWrapper.BLOCK){
					Block nestedBlock = (Block) branchNode;
					ASTNode nestedFirstStatement = (ASTNode) nestedBlock.statements().get(0);
					ASTNode nestedLastStatement = (ASTNode) nestedBlock.statements().get(nestedBlock.statements().size()-1);
					StatementWrapper nestedFirstItem = nodes.get(astMap.get(nestedFirstStatement));
					StatementWrapper nestedLastItem = nodes.get(astMap.get(nestedLastStatement));
					StatementWrapper lastItem = (StatementWrapper)nodes.get(astMap.get(branchNodes.get(i-1)));
					lastItem.setCFGSeqSuccessor(nestedFirstItem);
					StatementWrapper nextItem = (StatementWrapper)nodes.get(astMap.get(branchNodes.get(i+1)));
					nestedLastItem.setCFGSeqSuccessor(nextItem);
					buildGraphEdges(nestedBlock);
				}
				else{
					StatementWrapper branchNodeItem = nodes.get(astMap.get(branchNode));
					StatementWrapper nextBranchNodeItem;
					if(branchNodes.get(i+1).getNodeType()==StatementWrapper.BLOCK){
						Block nestedBlock = (Block)branchNodes.get(i+1);
						nextBranchNodeItem = nodes.get(astMap.get(nestedBlock.statements().get(0)));
					}
					else{
						nextBranchNodeItem = nodes.get(astMap.get(branchNodes.get(i+1)));
					}
					branchNodeItem.setCFGSeqSuccessor(nextBranchNodeItem);
					buildGraphEdges(branchNode);
				}
			}
			Statement lastBranchNode = branchNodes.get(i);
			if(lastBranchNode.getNodeType()==StatementWrapper.BLOCK){
				Block nestedBlock = (Block)lastBranchNode;
				StatementWrapper lastNestedBlockItem = nodes.get(astMap.get(nestedBlock.statements().get(nestedBlock.statements().size()-1)));
				lastNestedBlockItem.setCFGSeqSuccessor(switchItem.getCFGSeqSuccessor());
				buildGraphEdges(lastBranchNode);
			}
			else{
				StatementWrapper lastBranchNodeItem = nodes.get(astMap.get(lastBranchNode));
				lastBranchNodeItem.setCFGSeqSuccessor(switchItem.getCFGSeqSuccessor());
				buildGraphEdges(lastBranchNode);
			}
		}
		else if(nodeType == StatementWrapper.SYNCHRONIZED_STATEMENT){
			//This may not be handled correctly!
			Block syncBlock = ((SynchronizedStatement)(node)).getBody();
			if(syncBlock.statements().size()==0){
				return;
			}
			buildGraphEdges(syncBlock);
		}
		else if(nodeType == StatementWrapper.THROW_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.TRY_STATEMENT){
			Block tryBlock = ((TryStatement)(node)).getBody();
			if(tryBlock.statements().size()==0){
				return;
			}
			ASTNode firstStatement = (ASTNode) tryBlock.statements().get(0);
			ASTNode lastStatement = (ASTNode) tryBlock.statements().get(tryBlock.statements().size()-1);
			TryStatementWrapper tryItem = (TryStatementWrapper)nodes.get(astMap.get(node));
			StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
			StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
			lastItem.setCFGSeqSuccessor(tryItem.getCFGSeqSuccessor());
			tryItem.setCFGSeqSuccessor(firstItem);
			//Recursive
			buildGraphEdges(tryBlock);
		}
		else if(nodeType == StatementWrapper.TYPE_DECLARATION_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.VARIABLE_DECLARATION_STATEMENT){
			return;
		}
		else if(nodeType == StatementWrapper.WHILE_STATEMENT){
			//This may be problematic 
			WhileStatement whileNode = (WhileStatement) node;
			if(whileNode.getBody().getNodeType()==StatementWrapper.BLOCK){
				Block loopBlock = (Block)(whileNode.getBody());
				if(loopBlock.statements().size()==0){
					return;
				}
				
				ASTNode firstStatement = (ASTNode) loopBlock.statements().get(0);
				WhileStatementWrapper whileItem = (WhileStatementWrapper)nodes.get(astMap.get(node));
				StatementWrapper firstItem = nodes.get(astMap.get(firstStatement));
				whileItem.setBodyEntry(firstItem);
				
				ASTNode lastStatement = (ASTNode) loopBlock.statements().get(loopBlock.statements().size()-1);
				StatementWrapper lastItem = nodes.get(astMap.get(lastStatement));
				lastItem.setCFGSeqSuccessor(whileItem);
				//Recursive
				buildGraphEdges(loopBlock);
			}
			else{
				ASTNode bodyStatement = whileNode.getBody();
				StatementWrapper bodyItem = nodes.get(astMap.get(bodyStatement));
				WhileStatementWrapper whileItem = (WhileStatementWrapper)nodes.get(astMap.get(node));
				bodyItem.setCFGSeqSuccessor(whileItem);
				whileItem.setBodyEntry(bodyItem);
				buildGraphEdges(bodyStatement);
			}
		}
		else{
			System.out.println("Unexpected Type in CFG!("+nodeType+")");
		}
	}
	
	public MethodDeclaration getMethodDeclaration(){
		return this.methodASTNode;
	}
	
	public List<StatementWrapper> getNodes(){
		return this.nodes;
	}
	
	public void printCFG(){
		System.out.println("Nodes:");
		for(int i=0 ; i < nodes.size(); i++){
			StatementWrapper item = nodes.get(i);
			System.out.println("Node "+i+": <========");
			item.printDebug();
			System.out.println("========>");
		}
	}
}
