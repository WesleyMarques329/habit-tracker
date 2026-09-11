# Disciplina

Rastreador de hábito único (nofap) para Android, com grade de contribuições no estilo GitHub
e widget para tela inicial e tela de bloqueio.

## O que o app faz

- Grade de 53 semanas. Cada quadrado é um dia, do domingo ao sábado, igual ao GitHub.
- Toque em qualquer quadrado para marcar ou desmarcar aquele dia. Dá para preencher o passado.
- O verde fica mais forte conforme a sequência cresce: 1 a 2 dias, 3 a 6, 7 a 13, 14 ou mais.
- Sequência atual, recorde e total de dias.
- Widget com o número da sequência, um botão "marcar hoje" e uma mini grade das últimas 13 semanas.
- Tudo fica no aparelho. Sem conta, sem internet, sem permissões.

A sequência atual não zera enquanto o dia de hoje ainda não foi marcado. Ela conta a sequência que
termina ontem, para o dia em andamento não parecer uma recaída.

## Gerar o APK pelo GitHub Actions

1. Crie um repositório novo e vazio no GitHub, privado se preferir.
2. Nesta pasta, rode:

   ```bash
   git init
   git add .
   git commit -m "Primeira versao"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/SEU_REPO.git
   git push -u origin main
   ```

3. No GitHub, abra a aba **Actions**. O workflow "Build APK" começa sozinho após o push.
4. Quando terminar, abra a execução e baixe o artefato **disciplina-apk**. Vem em um zip.
5. Descompacte e transfira o `app-debug.apk` para o celular.

Se o build falhar, a aba Actions mostra o log do passo "Build debug APK".

## Instalar no Samsung S26

1. Copie o `app-debug.apk` para o celular, por cabo, Google Drive ou Quick Share.
2. Abra o arquivo pelo app Meus Arquivos.
3. O Android vai pedir para liberar a instalação de fontes desconhecidas para aquele app.
   Aceite e volte.
4. Confirme a instalação. O Play Protect pode avisar que o app é desconhecido. Escolha instalar
   mesmo assim, já que o APK foi compilado a partir deste código.

O APK é de debug e está assinado com a chave de debug padrão. Serve para uso pessoal.
Para publicar na Play Store seria preciso assinar com uma chave própria.

## Colocar o widget na tela de bloqueio

No One UI, a tela de bloqueio tem widgets próprios:

1. Toque e segure na tela de bloqueio, depois toque em **Bloq. tela e AOD**.
2. Escolha **Widgets** e adicione **Disciplina**.

Se a opção não aparecer no seu One UI, use a tela inicial: toque e segure em um espaço vazio,
escolha **Widgets**, procure **Disciplina** e arraste para a tela.

O widget tem dois tamanhos. Estreito mostra só o número e o botão. A partir de cerca de quatro
colunas de largura, aparece também a mini grade.

## Estrutura

| Arquivo | Papel |
| --- | --- |
| `app/src/main/java/com/wesley/nofap/data/HabitRepository.kt` | Armazena o conjunto de dias marcados no DataStore |
| `app/src/main/java/com/wesley/nofap/data/Stats.kt` | Sequência atual, recorde, intensidade e montagem das semanas |
| `app/src/main/java/com/wesley/nofap/ui/ContributionGrid.kt` | A grade estilo GitHub |
| `app/src/main/java/com/wesley/nofap/MainActivity.kt` | Tela principal |
| `app/src/main/java/com/wesley/nofap/widget/HabitWidget.kt` | Widget Glance e ação de marcar hoje |
| `.github/workflows/build.yml` | Compila o APK na nuvem |

## Abrir no Android Studio depois

O projeto não tem o `gradle-wrapper.jar`, porque ele é um binário e o build na nuvem usa o Gradle
instalado pelo runner. Ao abrir no Android Studio, escolha um Gradle local na primeira sincronização,
ou rode `gradle wrapper` uma vez na pasta do projeto.
