package org.scalalang.macroparadise
package typechecker

import org.scalalang.macroparadise.reflect.Enrichments

trait AnalyzerPlugins extends Compilers
                        with Namers
                        with Expanders
                        with Errors
                        with Enrichments
{
  import global._
  import analyzer._
  import analyzer.{Namer => NscNamer, AnalyzerPlugin => NscAnalyzerPlugin}

  object AnalyzerPlugin extends NscAnalyzerPlugin {
    override def pluginsEnterStats(typer: Typer, stats: List[Tree]): List[Tree] =
      mkExpander(typer.namer).expandMacroAnnotations(stats)

    override def pluginsEnterSym(namer: NscNamer, tree: Tree) =
      { mkNamer(namer).enterSym(tree); true }

    override def pluginsEnsureCompanionObject(namer: NscNamer, cdef: ClassDef, creator: ClassDef => Tree = companionModuleDef(_)) =
      Some(mkNamer(namer).ensureCompanionObject(cdef, creator))

    override def pluginsTypeSig(tpe: Type, typer: Typer, tree: Tree, pt: Type) = {
      tree match {
        case Template(_, _, body) =>
          mkExpander(typer.namer).expandMacroAnnotations(body)
        case cdef @ ClassDef(_, _, _, _) if { cdef.symbol.setInfo(tpe); treeInfo.isMacroAnnotation(cdef) } =>
          mkCompiler(typer).typedMacroAnnotation(cdef)
        case _ =>
          // do nothing
      }
      tpe
    }
  }
}
