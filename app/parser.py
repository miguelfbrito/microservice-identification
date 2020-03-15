import javalang
from Visitor import Visitor

code = """
package com.gaussic.controller;

@Entity
public class TestAController extends SomeStuff{

    public int something(){
        return 10;
    }
}



"""

tree = javalang.parse.parse(code)
print()


v = Visitor()
for path, node in tree:
    print("\nNODE: ")
    print(node)
    v.visit(node)
