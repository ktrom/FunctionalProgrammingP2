package varlang;
import static varlang.AST.*;
import static varlang.Value.*;

import java.util.List;
import java.util.ArrayList;

import varlang.AST.AddExp;
import varlang.AST.NumExp;
import varlang.AST.DivExp;
import varlang.AST.MultExp;
import varlang.AST.Program;
import varlang.AST.SubExp;
import varlang.AST.VarExp;
import varlang.AST.Visitor;
import varlang.Env.EmptyEnv;
import varlang.Env.ExtendEnv;

public class Evaluator implements Visitor<Value> {
	
	Value valueOf(Program p) {
		Env env = new Env.EmptyEnv();
		// Value of a program in this language is the value of the expression
		return (Value) p.accept(this, env);
	}
	
	@Override
	public Value visit(AddExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 0;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result += intermediate.v(); //Semantics of AddExp in terms of the target language.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(NumExp e, Env env) {
		return new NumVal(e.v());
	}

	@Override
	public Value visit(DivExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v(); 
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result / rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(MultExp e, Env env) {
		List<Exp> operands = e.all();
		double result = 1;
		for(Exp exp: operands) {
			NumVal intermediate = (NumVal) exp.accept(this, env); // Dynamic type-checking
			result *= intermediate.v(); //Semantics of MultExp.
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(Program p, Env env) {
		return (Value) p.e().accept(this, env);
	}

	@Override
	public Value visit(SubExp e, Env env) {
		List<Exp> operands = e.all();
		NumVal lVal = (NumVal) operands.get(0).accept(this, env);
		double result = lVal.v();
		for(int i=1; i<operands.size(); i++) {
			NumVal rVal = (NumVal) operands.get(i).accept(this, env);
			result = result - rVal.v();
		}
		return new NumVal(result);
	}

	@Override
	public Value visit(VarExp e, Env env) {
		// Previously, all variables had value 42. New semantics.
		return env.get(e.name());
	}	

	@Override
	public Value visit(LetExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());


		Env new_env = env;
		for (int i = 0; i < names.size(); i++) {
			values.add((Value)value_exps.get(i).accept(this, new_env));
			new_env = new ExtendEnv(new_env, names.get(i), values.get(i));
		}


		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(LeteExp e, Env env) { // New for varlang.
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Value> values = new ArrayList<Value>(value_exps.size());


		Env new_env = env;

		if(((NumVal)e.key().accept(this,new_env)).v() == 0){
			return new DynamicError("Divison by zero in expression " + visit(e, new_env));
		}

		for (int i = 0; i < names.size(); i++) {
			Value v = ((NumVal)value_exps.get(i).accept(this, new_env)).multVal((NumVal)e._key.accept(this, new_env));
			values.add(v);
			new_env = new ExtendEnv(new_env, names.get(i), values.get(i));
		}


		return (Value) e.body().accept(this, new_env);
	}

	@Override
	public Value visit(DecExp e, Env env) { // New for varlang.

		Env new_env = env;

		if(((NumVal)e.key().accept(this,new_env)).v() == 0){
			return new DynamicError("Divison by zero in expression " + visit(e, new_env));
		}
		Value v = ((NumVal)e._body.accept(this, new_env)).divVal((NumVal)e._key.accept(this, new_env));

		return v;
	}



}
