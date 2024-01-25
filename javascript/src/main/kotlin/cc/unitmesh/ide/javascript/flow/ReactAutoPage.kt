package cc.unitmesh.ide.javascript.flow

import cc.unitmesh.ide.javascript.util.JSPsiUtil
import com.intellij.lang.ecmascript6.JSXHarmonyFileType
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.TypeScriptJSXFileType
import com.intellij.lang.javascript.dialects.TypeScriptJSXLanguageDialect
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil
// keep this import
import kotlinx.serialization.json.Json

enum class RouterFile(val filename: String) {
    UMI(".umirc.ts"),
    NEXT("next.config.js"),
    VITE("vite.config.js"),
}

class ReactAutoPage(
    val project: Project,
    override var userTask: String,
    val editor: Editor
) : AutoPage {
    private val pages: MutableList<JSFile> = mutableListOf()
    private val components: MutableList<JSFile> = mutableListOf()

    // config files
    private val configs: MutableList<JSFile> = mutableListOf()

    init {
        val searchScope: GlobalSearchScope = ProjectScope.getContentScope(project)
        // todo: find .umirc.ts in root, find in modules
        val umirc = FileTypeIndex.getFiles(TypeScriptJSXFileType.INSTANCE, searchScope).firstOrNull {
            it.name == ".umirc.ts"
        }
        val psiManager = com.intellij.psi.PsiManager.getInstance(project)

        val virtualFiles =
            FileTypeIndex.getFiles(JavaScriptFileType.INSTANCE, searchScope) +
                    FileTypeIndex.getFiles(TypeScriptJSXFileType.INSTANCE, searchScope) +
                    FileTypeIndex.getFiles(JSXHarmonyFileType.INSTANCE, searchScope)

        val root = project.guessProjectDir()!!

        virtualFiles.forEach {
            val path = it.canonicalFile?.path ?: return@forEach

            val jsFile = (psiManager.findFile(it) ?: return@forEach) as? JSFile ?: return@forEach
            if (jsFile.isTestFile) return@forEach

            when {
                path.contains("pages") -> {
                    pages.add(jsFile)
                }

                path.contains("components") -> {
                    components.add(jsFile)
                }

                else -> {
                    if (root.findChild(it.name) != null) {
                        configs.add(jsFile)
                    }
                }
            }
        }

        println("pages: $pages")
    }

    override fun getRoutes(): List<String> {
        TODO("Not yet implemented")
    }

    override fun getPages(): List<DsComponent> = pages.mapNotNull {
        when (it.language) {
            is TypeScriptJSXLanguageDialect -> {
                Companion.tsxComponentToComponent(it)
            }

            else -> null
        }
    }
        .flatten()

    override fun getComponents(): List<DsComponent> {
        TODO("Not yet implemented")
    }

    // load prompts/context/ds.json from project root
    override fun getDesignSystemComponents(): List<DsComponent> {
        val rootConfig = project.guessProjectDir()
            ?.findChild("prompts")
            ?.findChild("context")
            ?.findChild("ds.json") ?: return emptyList()

        val json = rootConfig.inputStream.reader().readText()
        return try {
            val result: List<DsComponent> = Json.decodeFromString(json)
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun sampleRemoteCall(): String {
        TODO("Not yet implemented")
    }

    override fun sampleStateManagement(): String? {
        TODO("Not yet implemented")
    }

    override fun clarify(): String {
        TODO("Not yet implemented")
    }

    companion object {
        fun tsxComponentToComponent(jsFile: JSFile): List<DsComponent> {
            val exportElements = JSPsiUtil.getExportElements(jsFile)
            return exportElements.map { psiElement ->
                val name = psiElement.name ?: return@map null
                val path = jsFile.virtualFile.canonicalPath ?: return@map null
                // is React Functional Component
                return@map when (psiElement) {
                    is TypeScriptFunction -> {
                        DsComponent(name = name, path)
                    }

                    is TypeScriptClass -> {
                        DsComponent(name = name, path)
                    }

                    is TypeScriptVariable -> {
                        val funcExpr = PsiTreeUtil
                            .findChildrenOfType(psiElement, TypeScriptFunctionExpression::class.java)
                            .firstOrNull() ?: return@map null

                        val map = funcExpr.parameterList?.parameters?.mapNotNull { parameter ->
                            if (parameter.typeElement != null) {
                                parameter.typeElement
                            } else {
                                null
                            }
                        } ?: emptyList()

                        DsComponent(name = name, path)
                    }

                    else -> {
                        println("unknown type: ${psiElement::class.java}")
                        null
                    }
                }
            }.filterNotNull()
        }
    }
}