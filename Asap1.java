import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.io.BufferedInputStream;

public class Asap1 {
	private static Node graph[];
	private static SimpleStack stack;
	private static int nSCC             = 0;
	private static int biggestGroupSize = 0;
	private static int nClosedGroups    = 0;
	private static int index            = 0;

	private static class Node {
		private ArrayList<Integer> neighbors = new ArrayList<Integer>();
		private int index                    = -1;
		private int lowLink                  = 0;
		private int scc                      = 0;

		Node setIndex(int index) { this.index = index; return this; }
		Node setLowLink(int lowLink) { this.lowLink = lowLink; return this; }
		Node setScc(int scc) { this.scc = scc; return this; }

		int getIndex() { return this.index; }
		int getLowLink() { return this.lowLink; }
		int getScc() { return this.scc; }
		int getNeighborsSize() { return neighbors.size(); }
		ArrayList<Integer> getNeighbors() { return neighbors; }

		void addNeighbor(int neighbor) { neighbors.add(neighbor); }
	}

	private static class SimpleStack { 
		private int top = -1;
		private int topFake = -1;
		private int stack[];
		private boolean[] isInStack;

		public SimpleStack(int nNodes) {
			stack = new int[nNodes];
			isInStack = new boolean[nNodes];
			Arrays.fill(isInStack, Boolean.FALSE);
		}

		public void push(int node) {
			top++;
			topFake++;
			stack[top] = node;
			isInStack[node] = true;
		}

		public int pop() {
			int node = stack[top--];
			topFake--;
			isInStack[node] = false;
			return node;
		}

		public boolean isDisplaced() { return top != topFake; }
		public int popFake() { return stack[topFake--]; }
		public boolean contains(int node) { return isInStack[node]; }
		public int compensatePopFake() {
			int node = stack[top--];
			isInStack[node] = false;
			return node;
		}
	}

	public static void main(String args[]) throws IOException {
		int nNodes, nEdges, nodeBegin, nodeEnd, charIn;
		BufferedInputStream reader = new BufferedInputStream(System.in);
		long startTime, endInput, endTarjan;

		startTime = System.currentTimeMillis();
		nNodes = nEdges = 0;
		while ((charIn = reader.read()) != 32) {	// 32 = space
			nNodes *= 10;
			nNodes += charIn - 48;
		}
		while ((charIn = reader.read()) != 10) {	// 10 = newline
			nEdges *= 10;
			nEdges += charIn - 48;
		}

		graph = new Node[nNodes];
		stack = new SimpleStack(nNodes);
		for (int i = 0; i < nNodes; i++) { graph[i] = new Node(); }

		for (int i = 0; i < nEdges; i++) {
			nodeBegin = nodeEnd = 0;
			while ((charIn = reader.read()) != 32) {	// 32 = space
				nodeBegin *= 10;
				nodeBegin += charIn - 48;
			}
			while ((charIn = reader.read()) != 10) {	// 10 = newline
				nodeEnd *= 10;
				nodeEnd += charIn - 48;
			}
			--nodeBegin; --nodeEnd;
			graph[nodeBegin].addNeighbor(nodeEnd);
		}
		reader.close();
		endInput = System.currentTimeMillis() - startTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < nNodes; i++) {
			if (graph[i].getIndex() == -1) {
				strongConnect(i);
			}
		}
		endTarjan = System.currentTimeMillis() - startTime;

		System.out.println(nSCC);
		System.out.println(biggestGroupSize);
		System.out.println(nClosedGroups);
		System.out.println("Input time: " + endInput / 1000.0);
		System.out.println("Tarjan time: " + endTarjan / 1000.0);
	}

	private static void strongConnect(int node) {
		Node nodeCurrent = graph[node];
		int nodeLowLink = index, y;
		nodeCurrent.setIndex(index).setLowLink(index);
		index++;
		stack.push(node);

		Node neighbor;
		for (Integer i : nodeCurrent.getNeighbors()) {
			neighbor = graph[i];
			if (neighbor.getIndex() == -1) {
				strongConnect(i);
				y = neighbor.getLowLink();
				nodeLowLink = nodeLowLink > y ? y : nodeLowLink;
				nodeCurrent.setLowLink(nodeLowLink);
			} else if (stack.contains(i)) {
				y = neighbor.getIndex();
				nodeLowLink = nodeLowLink > y ? y : nodeLowLink;
				nodeCurrent.setLowLink(nodeLowLink);
			}
		}

		if (nodeLowLink == nodeCurrent.getIndex()) {
			int counter = 0;
			y = -1;			

			while (y != node) {
				y = stack.popFake();
				graph[y].setScc(nSCC);
				counter++;
			}

			boolean notClosed = false;
			while (stack.isDisplaced()) {
				y = stack.compensatePopFake();
				for (Integer i : graph[y].getNeighbors()) {
					if (graph[i].getScc() != nSCC) {
						notClosed = true;
						while (stack.isDisplaced()) { stack.compensatePopFake(); }
						break;
					}
				}
			}

			if (!notClosed) { ++nClosedGroups; }
			biggestGroupSize = counter > biggestGroupSize ? counter : biggestGroupSize;
			nSCC++;			
		}
	}
}