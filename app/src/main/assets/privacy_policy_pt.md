# Política de Privacidade do NavXS

Última atualização: 14 de setembro de 2026

## 1. Informações Gerais

O NavXS é uma aplicação Android que pode exibir um overlay de navegação com botões fixos para aplicações selecionadas.

O desenvolvedor desta aplicação recolhe, armazena ou processa dados pessoais apenas na medida tecnicamente necessária para o funcionamento da aplicação.

## 2. Processamento Local no Dispositivo

O NavXS processa geralmente as informações localmente no dispositivo.

Isso inclui em particular:

- o nome do pacote da aplicação atualmente em primeiro plano
- a seleção das aplicações para as quais o overlay deve ser ativado
- as definições do overlay definidas pelo utilizador, como posições, cores, tamanhos, transparência e temas
- o estado de ativação do serviço de acessibilidade
- o URI de uma imagem que o utilizador seleciona opcionalmente como fundo do editor de posicionamento, juntamente com a opacidade escolhida para ela

## 3. Finalidade do Processamento

Estas informações são processadas exclusivamente para:

- detetar se o overlay deve ser mostrado para a aplicação atualmente aberta
- mostrar ou ocultar o overlay
- fornecer as ações de navegação configuradas
- guardar e restaurar localmente as definições selecionadas pelo utilizador

## 4. Serviço de Acessibilidade

O NavXS utiliza a API do serviço de acessibilidade do Android (AccessibilityService). O NavXS não é uma ferramenta de acessibilidade para pessoas com deficiência; o serviço é necessário para a função principal do app, o overlay de navegação.

### 4.1 Consentimento

Antes de solicitar o serviço de acessibilidade, o NavXS mostra um aviso que explica como o serviço é utilizado. O usuário escolhe entre dois botões: "Recusar" e "Concordo". Somente após tocar em "Concordo" o NavXS abre as configurações de acessibilidade do Android, onde o usuário precisa ativar o serviço manualmente. "Recusar" fecha o app sem ativar nada. Sair do aviso de qualquer outra forma não é considerado consentimento. O serviço pode ser desativado a qualquer momento nas configurações de acessibilidade do Android.

### 4.2 Para que o serviço é utilizado

O serviço é utilizado exclusivamente para:

- detectar o nome do pacote do app que está em primeiro plano
- decidir se o overlay deve ser exibido para esse app
- exibir os botões de navegação flutuantes sobre os apps selecionados
- executar as ações de navegação Voltar, Início e Apps recentes quando o usuário toca no botão correspondente

### 4.3 O que o serviço não faz

Tecnicamente, o serviço de acessibilidade tem acesso às informações de janela do app em primeiro plano. O NavXS utiliza esse acesso apenas para determinar o nome do pacote desse app. O NavXS não lê, armazena nem transmite textos digitados, senhas, conteúdo de mensagens ou qualquer outro conteúdo da tela. O NavXS não executa ações em nome do usuário, exceto as ações de navegação mencionadas acima, e somente quando o usuário toca em um botão.

Este serviço não é utilizado para fins de publicidade, rastreamento ou análise, e os dados não são vendidos nem compartilhados.

## 5. Sem Transmissão para Servidores Externos

Atualmente, o NavXS não transmite as aplicações selecionadas, os nomes de pacotes de aplicações em primeiro plano nem as definições do overlay para servidores externos ou outros serviços externos.

O processamento ocorre localmente no dispositivo.

## 6. Armazenamento de Dados

As definições e seleções de aplicações armazenadas pelo NavXS são guardadas localmente no dispositivo.

Estes dados permanecem no dispositivo até serem modificados pelo utilizador, removidos ao limpar os dados da aplicação, ou eliminados ao desinstalar a aplicação.

Se o utilizador selecionar uma captura de ecrã como fundo do editor de posicionamento, o NavXS guarda apenas o URI dessa imagem, juntamente com o direito de leitura concedido pelo sistema. O próprio ficheiro de imagem não é copiado, não é guardado numa diretoria da app, não é entregue ao serviço de acessibilidade nem é transmitido. Ao repor o fundo, o URI guardado é eliminado e o direito de leitura é novamente libertado.

## 7. Permissões

O NavXS requer em particular a permissão de acessibilidade explicitamente ativada pelo utilizador para que a aplicação possa fornecer a sua funcionalidade de overlay.

Além disso, apenas são utilizados os acessos tecnicamente necessários para o funcionamento da aplicação.

## 8. Terceiros

Atualmente, o NavXS não utiliza serviços de terceiros, ferramentas de análise nem publicidade para processar os dados descritos acima.

## 9. Alterações a esta Política de Privacidade

Esta política de privacidade pode ser atualizada se necessário para refletir alterações à aplicação ou requisitos legais.

## 10. Contacto

Se tiver alguma questão sobre privacidade, pode contactar o desenvolvedor.

E-mail: navxs@robnice.de
