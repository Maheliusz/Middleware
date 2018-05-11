slice2java --output-dir generated slice/bank.ice
slice2py --output-dir src/bank/client slice/bank.ice
protoc.exe -I=. --java_out=generated --plugin=protoc-get-grpc-java=./protoc-gen-grpc-java.exe --grpc-java_out=generated money.proto
pause