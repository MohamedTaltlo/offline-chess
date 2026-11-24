import test from 'node:test';import assert from 'node:assert/strict';import {Chess} from 'chess.js';import {chooseMove} from '../src/engine.js';
test('legal moves and reversible history',()=>{const g=new Chess();assert.equal(g.moves().length,20);g.move('e4');g.move('e5');g.undo();g.undo();assert.equal(g.fen(),new Chess().fen());});
test('checkmate',()=>{const g=new Chess();for(const m of ['f3','e5','g4','Qh4#'])g.move(m);assert.equal(g.isCheckmate(),true);});
test('castling and en passant',()=>{const g=new Chess('r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1');g.move('O-O');assert.equal(g.get('f1').type,'r');const e=new Chess();for(const m of ['e4','a6','e5','d5','exd6'])e.move(m);assert.equal(e.get('d5'),undefined);});
test('promotion',()=>{const g=new Chess('7k/P7/8/8/8/8/8/7K w - - 0 1');g.move({from:'a7',to:'a8',promotion:'n'});assert.equal(g.get('a8').type,'n');});
test('computer generates legal move',()=>{const g=new Chess();for(const l of ['easy','medium','hard','expert'])assert.ok(g.moves().includes(chooseMove(g.fen(),l,80)));});
