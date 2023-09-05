type Student record {|
    string name;
    int age;
|};

public function main() {
    int x = 10;
    string y = "hello";
    Student s = test();
}

public function test() returns Student {
    return {name: "John", age: 20};
}
