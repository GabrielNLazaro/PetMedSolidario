# PetMed Solidário

Aplicativo Android de demonstração para tutores e estabelecimentos parceiros. O catálogo, os endereços e as identidades são fictícios. A experiência funciona localmente após a instalação. O acesso por perfil simula permissões e dispensa e-mail, senha e credenciais.

## Abrir e executar

1. Extraia o ZIP. No Android Studio, escolha **Open** e selecione a pasta **PetMedSolidario** que contém `settings.gradle.kts`.
2. Use uma versão do Android Studio compatível com o Android Gradle Plugin 8.7.3, como Ladybug 2024.2.1. Em **Settings → Build Tools → Gradle**, selecione um JDK 17 ou o JDK 21 incluído no Studio.
3. No **SDK Manager**, instale **Android SDK Platform 35**, **Android SDK Build-Tools 34.0.0** e **Android SDK Platform-Tools**. Aguarde a sincronização do Gradle. A primeira sincronização precisa de internet.
4. Crie um emulador Android 8.0 (API 26) ou superior no **Device Manager**, ou conecte um aparelho com depuração USB. Selecione o módulo **app** e clique em **Run ▶**.

### Compilar pelo terminal

Na raiz extraída, com o JDK configurado em `JAVA_HOME` e o SDK localizado pelo Android Studio ou pela variável `ANDROID_HOME`, execute no PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
```

No macOS ou Linux, use `sh ./gradlew :app:assembleDebug`. O APK de depuração será criado em `app/build/outputs/apk/debug/app-debug.apk`.

## Experimentar os fluxos

- **Tutor** — Entre como tutor, abra **Buscar**, digite “Amora” e selecione **Jardim Sul**. Abra o item e simule a reserva de uma unidade. Consulte **Atividades** e volte à busca para conferir o novo saldo.
- **Doação** — Em **Doar**, preencha um nome de exemplo, quantidade positiva, data futura no formato AAAA-MM-DD e ponto parceiro. Marque a declaração, revise os dados e confirme. O registro aparece como **Aguardando conferência** e fica fora do estoque disponível.
- **Pontos parceiros** — No fim da tela **Início**, abra a lista para consultar os dois endereços e horários fictícios.
- **Parceiro** — Em **Perfil**, saia e entre como parceiro. Abra **Perfil → Abrir painel do parceiro** para consultar as quantidades e os lotes da Clínica Ipê. O perfil de tutor não tem acesso a essa tela.

A sessão mantém as atividades ao alternar abas e girar a tela. Sair limpa os registros e restaura o estoque inicial. Não há banco persistente nem sincronização entre os perfis. Login real, mapas, fotos, triagem técnica e retiradas físicas ficam para etapas futuras. O aplicativo não solicita acesso à internet e não inclui Firebase, pagamentos ou serviços de doação.

## Organização

| Caminho | Conteúdo |
| --- | --- |
| `app/src/main/java/com/petmedsolidario/MainActivity.kt` | Entrada do aplicativo |
| `…/data/Catalog.kt` | Modelos, medicamentos e pontos de exemplo |
| `…/ui/AppNavigation.kt` | Navegação Compose, perfis, estoque e histórico da sessão |
| `…/ui/Screens.kt` | Acesso de demonstração, busca e formulário de doação |
| `…/ui/Components.kt` | Tema Material 3, cartões, títulos e botões |
| `app/src/main/res/` | Ícone de pata com cruz e tema de inicialização |
| `gradle/wrapper/` | Inicializador oficial e verificação da distribuição Gradle 8.9 |

A identidade usa verde #147D64, texto #173B30, fundo #F6F8F7, superfícies brancas e verde-claro #E9F5EF. Os cartões têm cantos de 16 dp. Dependências principais: Kotlin 2.0.21, Compose BOM 2024.12.01, Material 3 e Navigation Compose 2.8.5.

## Verificar o arquivo recebido

Na pasta que contém o ZIP, execute `Get-FileHash .\PetMedSolidario.zip -Algorithm SHA256` e compare o resultado com `PetMedSolidario.zip.sha256`, entregue ao lado do arquivo. Para validar a compilação no seu ambiente, execute o comando de build acima e confira a mensagem **BUILD SUCCESSFUL**.
