// This is a generated file. Not intended for manual editing.
package cc.unitmesh.devti.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static cc.unitmesh.devti.language.psi.DevInTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import cc.unitmesh.devti.language.psi.*;

public class DevInCodeContentsImpl extends ASTWrapperPsiElement implements DevInCodeContents {

  public DevInCodeContentsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DevInVisitor visitor) {
    visitor.visitCodeContents(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DevInVisitor) accept((DevInVisitor)visitor);
    else super.accept(visitor);
  }

}
