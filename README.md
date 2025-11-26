### Como compilar?

> javac -d bin src/architecture/*.java src/assembler/*.java src/components/*.java

O argumento "-d bin" envia todos os arquivos .class para o diretório bin. Só para organizar mesmo.

---

### Como rodar?

OBS: As extensões dos arquivos NÃO devem ser colocadas!

> java -cp bin assembler.Assembler <arquivo.dsf>

O arquivo .dsf é lido pelo assembler e transformado em um executável.

> java -cp bin architecture.Architecture <arquivo.dxf> <simulação>

O executável é lido instrução por instrução (caso simulação seja true) ou de uma só vez (caso simulação seja false ou não seja inserido).

O argumento "-cp bin" serve para rodar um arquivo que não está na pasta atual. Os comandos assumem que você está na pasta raiz do programa.
