# Parser

Este projeto representa um leitor de arquivos de configuração, em que o há um estilo próprio de configuração para este leitor de arquivos.
Este leitor está em desenvolvimento. Os **objetivos** que este projeto busca e reduzir o tamanho do arquivo com a `diminuição da quantidade de tokens` e, uma `maior velocidade de leitura` deste para torná-lo mais ágil.

#### Exemplo:
```Java
integer_number = 1, // Define um número inteiro
floating_point_number = 1.0, // Define um número ponto flutuante, outras maneiras aceitas de definir um ponto flutuante são (0., .0)
string = "A some string, test", // Define uma string.
array = [1, 2, 3, 4, 5, 1.25, "A string"], // Define uma lista dinâmica, em que os elementos desta lista podem ter diferentes tipos.
map = {1 = "A string", 2 = 2, 3 = 1.25}
```

#### TODO:
1. Lista reiteradas, ou seja, listas dentro de listas. ✅ **Versão 1.0.1**
2. Mapas reiterados, ou seja, mapas dentro de mapas.
3. Adição da leitura do valor booleano (**true** ou **false**). ✅ **Versão 1.0.1**
4. Comentários de linhas conjuntas.

## Versões
### 1.0.1 (26, Dezembro de 2018)
1. Adicionado a leitura de valores booleanos (**true** or **false**).
2. Adicionado a leitura de lista reiteradas, ou seja, listas dentro de listas.
3. Adição da leitura de valores numéricos com tipos explicitos.<br>
&nbsp;**3.1. Exemplo, caso eu queira declarar um valor numérico do tipo "byte", logo:<br>**
&nbsp;&nbsp;a = 1b -> A letra que declara o valor numérico do tipo byte pode ser "b" ou "B".<br>
&nbsp;**3.2. Tipos de valores númericos.**
      * ``b`` ou ``B``, declara o tipo **"byte"**.
      * ``s`` ou ``S``, declara o tipo **"short"**.
      * ``i`` ou ``I``, declara o tipo **"integer"**.
      * ``f`` ou ``F``, declara o tipo **"float"**.
      * ``d`` ou ``D``, declara o tipo **"double"**.
      * ``l`` ou ``L``, declara o tipo **"long"**.<br>
      **Obs: Se o número for de um tipo ``inteiro`` e não ter tipo explícito, logo o valor será do tipo "integer". Porém, caso o valor do
      número seja do tipo ``ponto-flutuante`` e não ter tipo explícito, logo o valor será do tipo "double".**
