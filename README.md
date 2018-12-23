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
1. Lista reiteradas, ou seja, listas dentro de listas.
2. Mapas reiterados, ou seja, mapas dentro de mapas.
3. Adição da leitura do valor booleano (**true** ou **false**).
