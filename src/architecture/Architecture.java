package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {
	
	private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
								//simulation mode shows the components' status after each instruction
	
	
	private boolean halt;

	private Bus extbus1;
	private Bus intbus1;
	
	private Memory memory;
	private Memory statusMemory;
	private int memorySize;
	
	private Register PC;
	private Register IR;
	private Register StkTOP;
	private Register StkBOT;

	private Register REG0;
	private Register REG1;
	private Register REG2;
	private Register REG3;

	private Register Flags;

	private Ula ula;
	private Demux demux; //only for multiple register purposes
	
	private ArrayList<String> commandsList;
	private ArrayList<Register> registersList;
	
	
	/**
	 * Instanciates all components in this architecture
	 */
	private void componentsInstances() {
		//don't forget the instantiation order
		//buses -> registers -> ula -> memory
		extbus1 = new Bus();
		intbus1 = new Bus();

		PC = new Register("PC", intbus1, intbus1);
		IR = new Register("IR", intbus1, intbus1);
		StkTOP = new Register("StkTOP", intbus1, intbus1);
		StkBOT = new Register("StkBOT", intbus1, intbus1);
		
		REG0 = new Register("REG0", intbus1, intbus1);
		REG1 = new Register ("REG1", intbus1, intbus1);
		REG2 = new Register("REG2", intbus1, intbus1);
		REG3 = new Register ("REG3", intbus1, intbus1);
		
		Flags = new Register(2, intbus1);

		fillRegistersList();

		ula = new Ula(extbus1, intbus1);
		
		statusMemory = new Memory(2, intbus1);
		memorySize = 128;
		memory = new Memory(memorySize, extbus1);
		
		demux = new Demux(); //this bus is used only for multiple register operations
		
		fillCommandsList();
	}

	/**
	 * This method fills the registers list inserting into them all the registers we have.
	 * IMPORTANT!
	 * The first register to be inserted must be the default REG
	 */
	private void fillRegistersList() {
		registersList = new ArrayList<Register>();
		registersList.add(REG0);
		registersList.add(REG1);
		registersList.add(REG2);
		registersList.add(REG3);

		registersList.add(PC);
		registersList.add(IR);
		registersList.add(StkTOP);
		registersList.add(StkBOT);
		
		registersList.add(Flags);
	}

	/**
	 * Constructor that instanciates all components according the architecture diagram
	 */
	public Architecture() {
		componentsInstances();
		
		//by default, the execution method is never simulation mode
		simulation = false;
	}

	
	public Architecture(boolean sim) {
		componentsInstances();
		
		//in this constructor we can set the simoualtion mode on or off
		simulation = sim;
	}



	//getters
	
	protected Bus getExtbus1() {
		return extbus1;
	}

	protected Bus getIntbus1() {
		return intbus1;
	}

	protected Memory getMemory() {
		return memory;
	}

	protected Register getPC() {
		return PC;
	}

	protected Register getIR() {
		return IR;
	}

	protected Register getStkTOP() {
		return StkTOP;
	}

	protected Register getStkBOT() {
		return StkBOT;
	}

	protected Register getREG0() {
		return REG0;
	}

	protected Register getREG1() {
		return REG1;
	}

	protected Register getREG2() {
		return REG2;
	}

	protected Register getREG3() {
		return REG3;
	}
	
	protected Register getFlags() {
		return Flags;
	}

	protected Ula getUla() {
		return ula;
	}

	public ArrayList<String> getCommandsList() {
		return commandsList;
	}

	//all the microprograms must be impemented here
	//tabela de instruções disponível em: https://sites.google.com/site/alvarodegas/degas-home-page/acad%C3%AAmico/disciplinas/2025-2/organiza%C3%A7%C3%A3o-e-arquitetura-de-computadores?authuser=0

	/**
	 * This method fills the commands list arraylist with all commands used in this architecture
	 */
	protected void fillCommandsList() {
		commandsList = new ArrayList<String>();
		commandsList.add("add_rr");   //0
		commandsList.add("add_mr");   //1
		commandsList.add("add_rm");   //2
		commandsList.add("add_imm");   //3

		commandsList.add("sub_rr");   //4
		commandsList.add("sub_mr");   //5
		commandsList.add("sub_rm");   //6
		commandsList.add("sub_imm");   //7

		commandsList.add("imul_mr");   //8
		commandsList.add("imul_rm");   //9
		commandsList.add("imul_rr");   //10

		commandsList.add("move_mr");   //11
		commandsList.add("move_rm");   //12
		commandsList.add("move_rr");   //13
		commandsList.add("move_imm");   //14

		commandsList.add("inc_r");   //15	

		commandsList.add("jmp");   //16
		commandsList.add("jn");    //17
		commandsList.add("jz");    //18

		commandsList.add("jeq");    //19
		commandsList.add("jneq");    //20

		commandsList.add("jgt");    //21
		commandsList.add("jlw");    //22
	}

	
	/**
	 * This method is used after some ULA operations, setting the flags bits according the result.
	 * @param result is the result of the operation
	 * NOT TESTED!!!!!!!
	 */
	private void setStatusFlags(int result) {
		Flags.setBit(0, 0);
		Flags.setBit(1, 0);
		if (result==0) { //bit 0 in flags must be 1 in this case
			Flags.setBit(0,1);
		}
		if (result<0) { //bit 1 in flags must be 1 in this case
			Flags.setBit(1,1);
		}
	}

	public void add_rr() {
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());  // RegID <- bus(int)
		registersRead();               // Reg(x) -> bus(int) (demux)
		IR.store();                    // IR <- bus(int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());  // RegID <- bus(int)
		registersRead();               // Reg(x) -> bus(int) (demux)
		ula.internalStore(1);          // ULA(1) <- bus (int)
		IR.read();                     // IR -> bus (int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.add();                     // ULA+
		ula.internalRead(1);           // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get());  // Flags
		registersStore();              // RegX <- bus (int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
	}
	
	public void add_mr(){
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		IR.store();                    // IR <- bus(int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());  // RegID <- bus(int)
		registersRead();               // Reg(x) -> bus (int) (demux)
		ula.internalStore(1);          // ULA(1) <- bus (int)
		IR.read();                     // IR -> bus (int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.add();                     // ULA+
		ula.internalRead(1);           // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get());  // Flags
		registersStore();              // RegX <- bus (int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
	}
	
	public void add_rm() {			   
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());  // RegID <- bus(int)
		registersRead();               // Reg(x) -> bus (int) (demux)
		IR.store();                    // IR <- bus(int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(1);                  // ULA(1) <- bus(ext)
		PC.read();                     // PC -> bus(int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		memory.store();                // Mem(store) <- bus(ext)
		IR.read();                     // IR -> bus (int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.add();                     // ULA+
		ula.read(1);                   // ULA(1) -> bus (ext)
		setStatusFlags(intbus1.get());  // Flags
		memory.store();                // Mem(store) <- bus(ext)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
	}

	public void add_imm() {			   
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		IR.store();                   // IR <- bus(int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		demux.setValue(intbus1.get()); // RegID <- bus(int)
		registersRead();              // Reg(x) -> bus (int) (demux)
		ula.internalStore(1);         // ULA(1) <- bus (int)
		IR.read();                    // IR -> bus (int)
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.add();                    // ULA+
		ula.internalRead(1);          // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get()); // Flags
		registersStore();             // RegX <- bus (int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
	}

	public void sub_rr() {				   
		PC.read();                         // PC -> bus(int)
		ula.internalStore(1);              // ULA(1) <- bus(int)
		ula.inc();                         // ULA++
		ula.internalRead(1);               // ULA(1) -> bus (int)
		PC.store();                        // PC <- bus(int)
		
		ula.internalStore(0);              // ULA(0) <- bus(int)
		ula.read(0);                       // ULA(0) -> bus(ext)
		memory.read();                     // Mem(r) <- bus(ext)
		ula.store(0);                      // ULA(0) <- bus(ext)
		ula.internalRead(0);               // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());      // RegID <- bus(int)
		registersRead();                   // Reg(x) -> bus(int) (demux)
		IR.store();                        // IR <- bus(int)
		
		PC.read();                         // PC -> bus(int)
		ula.internalStore(1);              // ULA(1) <- bus(int)
		ula.inc();                         // ULA++
		ula.internalRead(1);               // ULA(1) -> bus (int)
		PC.store();                        // PC <- bus(int)
		
		ula.internalStore(0);              // ULA(0) <- bus(int)
		ula.read(0);                       // ULA(0) -> bus(ext)
		memory.read();                     // Mem(r) <- bus(ext)
		ula.store(0);                      // ULA(0) <- bus(ext)
		ula.internalRead(0);               // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());      // RegID <- bus(int)
		registersRead();                   // Reg(x) -> bus (int) (demux)
		ula.internalStore(1);              // ULA(1) <- bus (int)
		IR.read();                         // IR -> bus (int)
		ula.internalStore(0);              // ULA(0) <- bus(int)
		ula.sub();                         // ULA-
		ula.internalRead(1);               // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get());      // Flags
		registersStore();                  // RegX <- bus (int)
		
		PC.read();                         // PC -> bus(int)
		ula.internalStore(1);              // ULA(1) <- bus(int)
		ula.inc();                         // ULA++
		ula.internalRead(1);               // ULA(1) -> bus (int)
		PC.store();                        // PC <- bus(int)
	}

	public void sub_mr() { 			  
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		IR.store();                   // IR <- bus(int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		demux.setValue(intbus1.get()); // RegID <- bus(int)
		registersRead();              // Reg(x) -> bus (int) (demux)
		ula.internalStore(1);         // ULA(1) <- bus (int)
		IR.read();                    // IR -> bus (int)
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.sub();                    // ULA-
		ula.internalRead(1);          // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get()); // Flags
		registersStore();             // RegX <- bus (int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
	}

	public void sub_rm() {			   
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(0);                  // ULA(0) <- bus(ext)
		ula.internalRead(0);           // ULA(0) -> bus(int)
		demux.setValue(intbus1.get());  // RegID <- bus(int)
		registersRead();               // Reg(x) -> bus (int) (demux)
		IR.store();                    // IR <- bus(int)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
		
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		ula.store(1);                  // ULA(1) <- bus(ext)
		PC.read();                     // PC -> bus(int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.read(0);                   // ULA(0) -> bus(ext)
		memory.read();                 // Mem(r) <- bus(ext)
		memory.store();                // Mem(store) <- bus(ext)
		IR.read();                     // IR -> bus (int)
		ula.internalStore(0);          // ULA(0) <- bus(int)
		ula.sub();                     // ULA-
		ula.read(1);                   // ULA(1) -> bus (ext)
		setStatusFlags(intbus1.get());  // Flags
		memory.store();                // Mem(store) <- bus(ext)
		
		PC.read();                     // PC -> bus(int)
		ula.internalStore(1);          // ULA(1) <- bus(int)
		ula.inc();                     // ULA++
		ula.internalRead(1);           // ULA(1) -> bus (int)
		PC.store();                    // PC <- bus(int)
	}
	
	public void sub_imm(){
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		IR.store();                   // IR <- bus(int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
		
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.read(0);                  // ULA(0) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(0);                 // ULA(0) <- bus(ext)
		ula.internalRead(0);          // ULA(0) -> bus(int)
		demux.setValue(intbus1.get()); // RegID <- bus(int)
		registersRead();              // Reg(x) -> bus (int) (demux)
		ula.internalStore(1);         // ULA(1) <- bus (int)
		IR.read();                    // IR -> bus (int)
		ula.internalStore(0);         // ULA(0) <- bus(int)
		ula.sub();                    // ULA-
		ula.internalRead(1);          // ULA(1) -> bus (int)
		setStatusFlags(intbus1.get()); // Flags
		registersStore();             // RegX <- bus (int)
		
		PC.read();                    // PC -> bus(int)
		ula.internalStore(1);         // ULA(1) <- bus(int)
		ula.inc();                    // ULA++
		ula.internalRead(1);          // ULA(1) -> bus (int)
		PC.store();                   // PC <- bus(int)
	}

	public void imul_mr(){
		
	}

	public void imul_rm(){
		
	}

	public void imul_rr(){
		
	}

	public void move_mr() { // MOVE mem -> reg
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the value in memory
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)
		IR.store();           // IR <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the register id
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)

		demux.setValue(intbus1.get()); // RegID <- bus(int)
		IR.read();                    // IR -> bus(int)
		registersStore();             // Reg(x) <- bus(int) (demux)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)
	}

	public void move_rm() { // MOVE reg -> mem
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the register id, and read the value of said register
		// the new PC value is already on ULA(1)
		ula.read(1);                  // ULA(1) -> bus(ext)
		memory.read();                // Mem(r) <- bus(ext)
		ula.store(1);                 // ULA(1) <- bus(ext)
		ula.internalRead(1);          // ULA(1) -> bus(int)
		demux.setValue(intbus1.get()); // RegID <- bus(int)
		registersRead();              // Reg(x) -> bus(int) (demux)
		IR.store();                   // IR <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the memory address, and feed it back in store mode
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		memory.store();       // Mem(s) <- bus(ext)
		IR.read();            // IR -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.store();       // Mem(s) <- bus(ext)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)
	}

	public void move_rr() { // MOVE regA -> regB
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get regA id
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)

		// read value of regA and put it in IR
		demux.setValue(intbus1.get()); // RegID <- bus(int)
		registersRead() ;             // Reg(x) -> bus(int) (demux)
		IR.store();                   // IR <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get regB id
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)

		demux.setValue(intbus1.get()); // RegID <- bus(int)
		IR.read();                    // IR -> bus(int)
		registersStore();             // Reg(x) <- bus(int) (demux)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)
	}

	public void move_imm() { // MOVE immediate -> reg
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the immediate value and put it in IR
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)
		IR.store();           // IR <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the register ID and put it in the internal bus
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)

		demux.setValue(intbus1.get()); // RegID <- bus(int)
		IR.read();                    // IR -> bus(int)
		registersStore();             // Reg(x) <- bus(int) (demux)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)
	}

	public void inc_r() {
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		ula.internalStore(0);
		ula.read(0);
		memory.read();
		ula.store(0);
		ula.internalRead(0);
		demux.setValue(intbus1.get());
		registersRead();

		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		setStatusFlags(intbus1.get());
		registersStore();

		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();
	}

	public void jmp() {
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the value in memory
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)
		IR.store();           // IR <- bus(int)
		PC.store();           // PC <- bus(int)
	}

	public void jn() {
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the jump address and put it in Status(1)
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)
		statusMemory.storeIn1(); // Status(1) <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// put the not-jump address in Status(0)
		statusMemory.storeIn0(); // Status(0) <- bus(int)

		intbus1.put(Flags.getBit(1));
		statusMemory.read();
		PC.store();
	}

	public void jz() {
		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// get the jump address and put it in Status(1)
		// the new PC value is already on ULA(1)
		ula.read(1);          // ULA(1) -> bus(ext)
		memory.read();        // Mem(r) <- bus(ext)
		ula.store(1);         // ULA(1) <- bus(ext)
		ula.internalRead(1);  // ULA(1) -> bus(int)
		statusMemory.storeIn1(); // Status(1) <- bus(int)

		// pc++
		PC.read();            // PC -> bus(int)
		ula.internalStore(1); // ULA(1) <- bus(int)
		ula.inc();            // ULA++
		ula.internalRead(1);  // ULA(1) -> bus(int)
		PC.store();           // PC <- bus(int)

		// put the not-jump address in Status(0)
		statusMemory.storeIn0(); // Status(0) <- bus(int)

		intbus1.put(Flags.getBit(0));
		statusMemory.read();
		PC.store();
	}

	public void jeq() {
		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regA id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the id and read the specified register's value, then store it into IR
		demux.setValue(intbus1.get());
		registersRead();
		IR.store();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regB id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the regB id and read the specified register's value, then store it into ula(1)
		demux.setValue(intbus1.get());
		registersRead();
		ula.internalStore(1);

		// get regA's value (from IR) and put it into ula(0)
		IR.read();
		ula.internalStore(0);

		// perform a subtraction and update the flags register
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus1.get());

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// get jump address from memory, and put it into ula(0)
		ula.read(1);
		memory.read();
		ula.store(0);

		// put the address in the status memory (slot 1, when the values were equal)
		ula.internalRead(0);
		statusMemory.storeIn1();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// put the address of the next instruction in the status memory (slot 0, when the values were different)
		PC.read();
		statusMemory.storeIn0();

		// jump to the address (based on the zero flag)
		intbus1.put(Flags.getBit(0));
		statusMemory.read();
		PC.store();
	}

	public void jneq(){
		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regA id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the id and read the specified register's value, then store it into IR
		demux.setValue(intbus1.get());
		registersRead();
		IR.store();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regB id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the regB id and read the specified register's value, then store it into ula(1)
		demux.setValue(intbus1.get());
		registersRead();
		ula.internalStore(1);

		// get regA's value (from IR) and put it into ula(0)
		IR.read();
		ula.internalStore(0);

		// perform a subtraction and update the flags register
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus1.get());

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// get jump address from memory, and put it into ula(0)
		ula.read(1);
		memory.read();
		ula.store(0);

		// put the address in the status memory (slot 1, when the values were equal)
		ula.internalRead(0);
		statusMemory.storeIn0();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// put the address of the next instruction in the status memory (slot 0, when the values were equal)
		PC.read();
		statusMemory.storeIn1();

		// jump to the address (based on the zero flag)
		intbus1.put(Flags.getBit(0));
		statusMemory.read();
		PC.store();
	}

	public void jgt() {
		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regA id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the id and read the specified register's value, then store it into IR
		demux.setValue(intbus1.get());
		registersRead();
		IR.store();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regB id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the regB id and read the specified register's value, then store it into ula(0)
		demux.setValue(intbus1.get());
		registersRead();
		ula.internalStore(0);

		// get regA's value (from IR) and put it into ula(1)
		IR.read();
		ula.internalStore(1);

		// perform a subtraction and update the flags register
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus1.get());

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// get jump address from memory, and put it into ula(0)
		ula.read(1);
		memory.read();
		ula.store(0);

		// put the address in the status memory (slot 1, when regA>regB)
		ula.internalRead(0);
		statusMemory.storeIn1();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// put the address of the next instruction in the status memory (slot 0, when regA<=regB)
		PC.read();
		statusMemory.storeIn0();

		// jump to the address (based on the negative flag)
		intbus1.put(Flags.getBit(1));
		statusMemory.read();
		PC.store();
	}

	public void jlw() {
		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regA id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the id and read the specified register's value, then store it into IR
		demux.setValue(intbus1.get());
		registersRead();
		IR.store();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// read regB id from memory and put it on intBus
		ula.read(1);
		memory.read();
		ula.store(0);
		ula.internalRead(0);

		// get the regB id and read the specified register's value, then store it into ula(1)
		demux.setValue(intbus1.get());
		registersRead();
		ula.internalStore(1);

		// get regA's value (from IR) and put it into ula(0)
		IR.read();
		ula.internalStore(0);

		// perform a subtraction and update the flags register
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus1.get());

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();

		// get jump address from memory, and put it into ula(0)
		ula.read(1);
		memory.read();
		ula.store(0);

		// put the address in the status memory (slot 0, when regA>regB)
		ula.internalRead(0);
		statusMemory.storeIn1();

		// pc++
		PC.read();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		PC.store();
		
		// put the address of the next instruction in the status memory (slot 1, when regA<=regB)
		PC.read();
		statusMemory.storeIn0();

		// jump to the address (based on the negative flag)
		intbus1.put(Flags.getBit(1));
		statusMemory.read();
		PC.store();
	}
	
	public ArrayList<Register> getRegistersList() {
		return registersList;
	}

	/**
	 * This method performs an (external) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersRead() {
		registersList.get(demux.getValue()).read();
	}
	
	/**
	 * This method performs an (internal) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalRead() {
		registersList.get(demux.getValue()).internalRead();;
	}
	
	/**
	 * This method performs an (external) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersStore() {
		registersList.get(demux.getValue()).store();
	}
	
	/**
	 * This method performs an (internal) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalStore() {
		registersList.get(demux.getValue()).internalStore();;
	}



	/**
	 * This method reads an entire file in machine code and
	 * stores it into the memory
	 * NOT TESTED
	 * @param filename
	 * @throws IOException 
	 */
	public void readExec(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new		 
		   FileReader(filename+".dxf"));
		   String linha;
		   int i=0;
		   while ((linha = br.readLine()) != null) {
			     extbus1.put(i);
			     memory.store();
			   	 extbus1.put(Integer.parseInt(linha));
			     memory.store();
			     i++;
			}
			br.close();
	}
	
	/**
	 * This method executes a program that is stored in the memory
	 */
	public void controlUnitEexec() {
		halt = false;
		while (!halt) {
			fetch();
			decodeExecute();
		}

	}
	

	/**
	 * This method implements The decode proccess,
	 * that is to find the correct operation do be executed
	 * according the command.
	 * And the execute proccess, that is the execution itself of the command
	 */
	private void decodeExecute() {
		IR.internalRead();
		int command = intbus1.get();
		System.out.println(intbus1.get());
		simulationDecodeExecuteBefore(command);
		switch (command) {
		case 0: add_rr(); break;
		case 1: add_mr(); break;
		case 2: add_rm(); break;
		case 3: add_imm(); break;

		case 4: sub_rr(); break;
		case 5: sub_mr(); break;
		case 6: sub_rm(); break;
		case 7: sub_imm(); break;

		case 8: imul_mr(); break;
		case 9: imul_rm(); break;
		case 10: imul_rr(); break;

		case 11: move_mr(); break;
		case 12: move_rm(); break;
		case 13: move_rr(); break;
		case 14: move_imm(); break;

		case 15: inc_r(); break;

		case 16: jmp(); break;
		case 17: jn(); break;
		case 18: jz(); break;

		case 19: jeq(); break;
		case 20: jneq(); break;

		case 21: jgt(); break;
		case 22: jlw(); break;

		default: halt = true; break;

		}
		if (simulation)
			simulationDecodeExecuteAfter();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 * @param command 
	 */
	private void simulationDecodeExecuteBefore(int command) {
		System.out.println("----------BEFORE Decode and Execute phases--------------");
		String instruction;
		int parameter1 = 0;
		int parameter2 = 0;
		int parameter3 = 0;

		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		if (command !=-1)
			instruction = commandsList.get(command);
		else
			instruction = "END";
		if (hasRRformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			parameter2 = memory.getDataList()[PC.getData()+2];
			
			System.out.println("Instruction: "+instruction+" %REG"+parameter1 + " %REG"+parameter2);
		}
		if (hasMRformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			parameter2 = memory.getDataList()[PC.getData()+2];
			
			System.out.println("Instruction: "+instruction+" mem["+parameter1+"] %REG"+parameter2);
		}
		if (hasRMformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			parameter2 = memory.getDataList()[PC.getData()+2];
			
			System.out.println("Instruction: "+instruction+" %REG"+parameter1 + " mem["+parameter2+"]");
		}
		if (hasIMMformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			parameter2 = memory.getDataList()[PC.getData()+2];
			
			System.out.println("Instruction: "+instruction+" "+parameter1 + " %REG"+parameter2);
		}
		if (hasRformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			
			System.out.println("Instruction: "+instruction+" %REG"+parameter1);
		}
		if (hasMformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			
			System.out.println("Instruction: "+instruction+" mem["+parameter1+"]");
		}
		if (hasRRMformat(instruction)) {
			parameter1 = memory.getDataList()[PC.getData()+1];
			parameter2 = memory.getDataList()[PC.getData()+2];
			parameter3 = memory.getDataList()[PC.getData()+3];
			
			System.out.println("Instruction: "+instruction+" %REG"+parameter1 + " %REG"+parameter2+" mem["+parameter3+"]");
		}		
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED 
	 */
	private void simulationDecodeExecuteAfter() {
		String instruction;
		System.out.println("-----------AFTER Decode and Execute phases--------------");
		System.out.println("Internal Bus 1: "+intbus1.get());
		System.out.println("External Bus 1: "+extbus1.get());
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		Scanner entrada = new Scanner(System.in);
		System.out.println("Press <Enter>");
		String mensagem = entrada.nextLine();
	}

	/**
	 * This method uses PC to find, in the memory,
	 * the command code that must be executed.
	 * This command must be stored in IR
	 * NOT TESTED!
	 */
	private void fetch() {
		PC.read();

		ula.internalStore(1);
		ula.read(1);
		
		memory.read();

		ula.store(1);
		ula.internalRead(1);

		IR.store();

		simulationFetch();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED!!!!!!!!!
	 */
	private void simulationFetch() {
		if (simulation) {
			System.out.println("-------Fetch Phase------");
			System.out.println("PC: "+PC.getData());
			System.out.println("IR: "+IR.getData());
		}
	}

	/**
	 * This method is used to show in a correct way the operands (if there is any) of instruction,
	 * when in simulation mode
	 * NOT TESTED!!!!!
	 * @param instruction 
	 * @return
	 */
	private boolean hasOperands(String instruction) {
		if ("inc".equals(instruction))
			return false;
		else
			return true;
	}

	private boolean hasRRformat(String instruction) {
		if ("add_rr".equals(instruction)) return true;
		if ("sub_rr".equals(instruction)) return true;
		if ("imul_rr".equals(instruction)) return true;
		if ("move_rr".equals(instruction)) return true;

		return false;
	}

	private boolean hasMRformat(String instruction) {
		if ("add_mr".equals(instruction)) return true;
		if ("sub_mr".equals(instruction)) return true;
		if ("imul_mr".equals(instruction)) return true;
		if ("move_mr".equals(instruction)) return true;

		return false;
	}

	private boolean hasRMformat(String instruction) {
		if ("add_rm".equals(instruction)) return true;
		if ("sub_rm".equals(instruction)) return true;
		if ("imul_rm".equals(instruction)) return true;
		if ("move_rm".equals(instruction)) return true;

		return false;
	}

	private boolean hasIMMformat(String instruction) {
		if ("add_imm".equals(instruction)) return true;
		if ("sub_imm".equals(instruction)) return true;
		if ("move_imm".equals(instruction)) return true;

		return false;
	}

	private boolean hasRformat(String instruction) {
		if ("inc_r".equals(instruction)) return true;

		return false;
	}

	private boolean hasMformat(String instruction) {
		if ("jmp".equals(instruction)) return true;
		if ("jn".equals(instruction)) return true;
		if ("jz".equals(instruction)) return true;

		return false;
	}

	private boolean hasRRMformat(String instruction) {
		if ("jeq".equals(instruction)) return true;
		if ("jneq".equals(instruction)) return true;
		if ("jgt".equals(instruction)) return true;
		if ("jlw".equals(instruction)) return true;

		return false;
	}

	/**
	 * This method returns the amount of positions allowed in the memory
	 * of this architecture
	 * NOT TESTED!!!!!!!
	 * @return
	 */
	public int getMemorySize() {
		return memorySize;
	}
	
	public static void main(String[] args) throws IOException {
		Architecture arch = new Architecture(true);
		arch.readExec("program");
		arch.controlUnitEexec();
	}
	

}
