package com.github.xepozz.ide.introspector.core

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiField
import com.intellij.psi.PsiImportStatementBase
import com.intellij.psi.PsiLabeledStatement
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiVariable

/**
 * Pure (no read action required for classification — just inspection of PSI types) classifier
 * that maps a [PsiElement] to the kind taxonomy shared by `psi.symbol_at` and `psi.get_outline`:
 *
 * `class | interface | enum | annotation | record | object | companion |
 *  method | constructor | field | property | parameter | variable |
 *  typeAlias | enumConstant | import | label | unknown`
 *
 * Java PSI is bundled with the IntelliJ platform and reachable at compile time. Kotlin PSI
 * lives in an optional plugin (`org.jetbrains.kotlin`) — exactly the same dependency model
 * as [com.github.xepozz.ide.introspector.exec.ExecToolset] — so Kotlin classification goes
 * through string-based class-name dispatch. Doing reflection through `Class.forName` would
 * defeat plugin-verifier checks; this is just enough to recognize `KtClass` / `KtNamedFunction`
 * etc. when they walk past.
 *
 * The classifier also extracts `name`, `fqn`, `modifiers`, `returnType` and `typeText` since
 * those are derived from the same PSI inspection — keeping it all in one place avoids the
 * "kind says class but FQN extraction missed it" inconsistency.
 */
object PsiKindClassifier {

    /** What a callsite needs to render a symbol — bundled because every field is derived
     *  from the same set of PSI casts / reflective name lookups. */
    data class Classified(
        val kind: String,
        val name: String?,
        val fqn: String?,
        val modifiers: List<String>,
        val returnType: String?,
        val typeText: String?,
    )

    fun classify(element: PsiElement): Classified {
        return classifyJava(element) ?: classifyKotlin(element) ?: Classified(
            kind = "unknown",
            name = (element as? PsiNamedElement)?.name,
            fqn = null,
            modifiers = emptyList(),
            returnType = null,
            typeText = null,
        )
    }

    /** Shortcut when callers only care about the kind. */
    fun kindOf(element: PsiElement): String = classify(element).kind

    // ---------- Java ----------

    private fun classifyJava(element: PsiElement): Classified? = when (element) {
        is PsiClass -> Classified(
            kind = when {
                element.isAnnotationType -> "annotation"
                element.isEnum -> "enum"
                element.isInterface -> "interface"
                element.isRecord -> "record"
                else -> "class"
            },
            name = element.name,
            fqn = element.qualifiedName,
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.CLASS),
            returnType = null,
            typeText = null,
        )

        is PsiMethod -> Classified(
            kind = if (element.isConstructor) "constructor" else "method",
            name = element.name,
            fqn = fqnOfMember(element.containingClass?.qualifiedName, element.name),
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.METHOD),
            returnType = element.returnType?.canonicalText,
            typeText = null,
        )

        is PsiEnumConstant -> Classified(
            kind = "enumConstant",
            name = element.name,
            fqn = fqnOfMember(element.containingClass?.qualifiedName, element.name),
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.FIELD),
            returnType = null,
            typeText = element.type.canonicalText,
        )

        is PsiField -> Classified(
            kind = "field",
            name = element.name,
            fqn = fqnOfMember(element.containingClass?.qualifiedName, element.name),
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.FIELD),
            returnType = null,
            typeText = element.type.canonicalText,
        )

        is PsiParameter -> Classified(
            kind = "parameter",
            name = element.name,
            fqn = null,
            modifiers = (element as PsiModifierListOwner).modifierList?.let { ml ->
                PsiModifiers.read(ml, PsiModifiers.FIELD)
            } ?: emptyList(),
            returnType = null,
            typeText = element.type.canonicalText,
        )

        is PsiLocalVariable -> Classified(
            kind = "variable",
            name = element.name,
            fqn = null,
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.FIELD),
            returnType = null,
            typeText = element.type.canonicalText,
        )

        is PsiClassInitializer -> Classified(
            kind = "method",
            name = element.name ?: "<clinit>",
            fqn = null,
            modifiers = PsiModifiers.read(element.modifierList, PsiModifiers.METHOD),
            returnType = null,
            typeText = null,
        )

        is PsiImportStatementBase -> Classified(
            kind = "import",
            name = null,
            fqn = null,
            modifiers = emptyList(),
            returnType = null,
            typeText = null,
        )

        is PsiLabeledStatement -> Classified(
            kind = "label",
            name = element.name,
            fqn = null,
            modifiers = emptyList(),
            returnType = null,
            typeText = null,
        )

        // Catch-all for Java PSI nodes with a type element (rare standalone — keep last).
        is PsiTypeElement -> null

        // Fallthrough: any other Java PsiVariable subtype (defensive — most concrete types
        // are handled above, but the abstract hierarchy is open).
        is PsiVariable -> Classified(
            kind = "variable",
            name = element.name,
            fqn = null,
            modifiers = emptyList(),
            returnType = null,
            typeText = element.type.canonicalText,
        )

        else -> null
    }

    private fun fqnOfMember(ownerFqn: String?, memberName: String?): String? {
        if (ownerFqn.isNullOrBlank() || memberName.isNullOrBlank()) return null
        return "$ownerFqn.$memberName"
    }

    // ---------- Kotlin (reflective — module is optional) ----------

    /**
     * Kotlin PSI lives in an optional plugin, so we never import the classes statically.
     * Instead we dispatch on the simple class name (`KtClass`, `KtNamedFunction`, …) and use
     * reflection on stable member names. Failure → null so the public caller falls through to
     * `unknown`.
     */
    private fun classifyKotlin(element: PsiElement): Classified? {
        val simpleName = element.javaClass.simpleName
        if (!simpleName.startsWith("Kt")) return null
        return try {
            when (simpleName) {
                "KtClass" -> kotlinClass(element)
                "KtObjectDeclaration" -> kotlinObject(element)
                "KtNamedFunction" -> kotlinFunction(element)
                "KtSecondaryConstructor", "KtPrimaryConstructor" -> kotlinConstructor(element)
                "KtProperty" -> kotlinProperty(element)
                "KtParameter" -> kotlinParameter(element)
                "KtDestructuringDeclarationEntry" -> kotlinSimple(element, "variable")
                "KtTypeAlias" -> kotlinTypeAlias(element)
                "KtEnumEntry" -> Classified(
                    kind = "enumConstant",
                    name = invokeStringSafe(element, "getName"),
                    fqn = ktFqName(element),
                    modifiers = ktModifiers(element),
                    returnType = null,
                    typeText = null,
                )
                "KtClassInitializer" -> Classified(
                    kind = "method",
                    name = "<init>",
                    fqn = null,
                    modifiers = emptyList(),
                    returnType = null,
                    typeText = null,
                )
                "KtImportDirective" -> Classified(
                    kind = "import",
                    name = invokeStringSafe(element, "getAliasName"),
                    fqn = null,
                    modifiers = emptyList(),
                    returnType = null,
                    typeText = null,
                )
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun kotlinClass(element: PsiElement): Classified {
        val isInterface = invokeBoolSafe(element, "isInterface")
        val isAnnotation = invokeBoolSafe(element, "isAnnotation")
        val isEnum = invokeBoolSafe(element, "isEnum")
        val isData = invokeBoolSafe(element, "isData")
        val kind = when {
            isAnnotation -> "annotation"
            isEnum -> "enum"
            isInterface -> "interface"
            isData -> "class" // data classes are still class kind
            else -> "class"
        }
        return Classified(
            kind = kind,
            name = invokeStringSafe(element, "getName"),
            fqn = ktFqName(element),
            modifiers = ktModifiers(element),
            returnType = null,
            typeText = null,
        )
    }

    private fun kotlinObject(element: PsiElement): Classified {
        val isCompanion = invokeBoolSafe(element, "isCompanion")
        return Classified(
            kind = if (isCompanion) "companion" else "object",
            name = invokeStringSafe(element, "getName"),
            fqn = ktFqName(element),
            modifiers = ktModifiers(element),
            returnType = null,
            typeText = null,
        )
    }

    private fun kotlinFunction(element: PsiElement): Classified = Classified(
        kind = "method",
        name = invokeStringSafe(element, "getName"),
        fqn = ktFqName(element),
        modifiers = ktModifiers(element),
        returnType = null,  // resolving return type needs the Kotlin analyzer — skipped to keep symbol_at cheap
        typeText = null,
    )

    private fun kotlinConstructor(element: PsiElement): Classified = Classified(
        kind = "constructor",
        name = invokeStringSafe(element, "getName") ?: "<init>",
        fqn = null,
        modifiers = ktModifiers(element),
        returnType = null,
        typeText = null,
    )

    private fun kotlinProperty(element: PsiElement): Classified {
        val typeRef = invokeSafe(element, "getTypeReference")
        val typeText = typeRef?.let { invokeStringSafe(it, "getText") }
        return Classified(
            kind = "property",
            name = invokeStringSafe(element, "getName"),
            fqn = ktFqName(element),
            modifiers = ktModifiers(element),
            returnType = null,
            typeText = typeText,
        )
    }

    private fun kotlinParameter(element: PsiElement): Classified {
        val typeRef = invokeSafe(element, "getTypeReference")
        val typeText = typeRef?.let { invokeStringSafe(it, "getText") }
        return Classified(
            kind = "parameter",
            name = invokeStringSafe(element, "getName"),
            fqn = null,
            modifiers = ktModifiers(element),
            returnType = null,
            typeText = typeText,
        )
    }

    private fun kotlinTypeAlias(element: PsiElement): Classified = Classified(
        kind = "typeAlias",
        name = invokeStringSafe(element, "getName"),
        fqn = ktFqName(element),
        modifiers = ktModifiers(element),
        returnType = null,
        typeText = null,
    )

    private fun kotlinSimple(element: PsiElement, kind: String): Classified = Classified(
        kind = kind,
        name = invokeStringSafe(element, "getName"),
        fqn = null,
        modifiers = emptyList(),
        returnType = null,
        typeText = null,
    )

    /** Kotlin's `KtNamedDeclaration.fqName: FqName?` — call via reflection. */
    private fun ktFqName(element: PsiElement): String? {
        val fqName = invokeSafe(element, "getFqName") ?: return null
        return invokeStringSafe(fqName, "asString")
    }

    /**
     * Kotlin uses `KtModifierList` — same `hasModifier` shape as Java's `PsiModifierList`,
     * but the constants are `KtTokens.*`. We avoid the whole token machinery and just read
     * the modifier-list text and grep for known keywords — good enough for `psi.symbol_at`,
     * which doesn't need byte-perfect parity with Kotlin's own modifier set.
     */
    private fun ktModifiers(element: PsiElement): List<String> {
        val modifierList = invokeSafe(element, "getModifierList") ?: return emptyList()
        val text = invokeStringSafe(modifierList, "getText") ?: return emptyList()
        val tokens = text.split(Regex("[\\s\\n]+")).filter { it.isNotBlank() }
        val recognized = setOf(
            "public", "protected", "private", "internal",
            "abstract", "final", "open", "sealed",
            "override", "lateinit", "const", "inline", "infix", "operator",
            "suspend", "tailrec", "data", "enum", "annotation",
            "companion", "inner", "external", "out", "in", "noinline", "crossinline",
            "vararg", "actual", "expect", "fun",
        )
        return tokens.filter { it in recognized }
    }

    private fun invokeSafe(receiver: Any, methodName: String): Any? = try {
        val m = receiver.javaClass.methods.firstOrNull { it.name == methodName && it.parameterCount == 0 }
        m?.invoke(receiver)
    } catch (_: Throwable) {
        null
    }

    private fun invokeStringSafe(receiver: Any, methodName: String): String? =
        invokeSafe(receiver, methodName) as? String

    private fun invokeBoolSafe(receiver: Any, methodName: String): Boolean =
        (invokeSafe(receiver, methodName) as? Boolean) ?: false
}
