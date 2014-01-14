/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.util.swing;

import imagej.util.awt.AWTWindows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A resizable dialog box containing a particular component.
 * <p>
 * It is very similar to the dialog generated by
 * {@link JOptionPane#showConfirmDialog(Component, Object, String, int, int)},
 * except that it has a few extra features:
 * </p>
 * <ul>
 * <li>It can limit the size of the dialog based on the actual screen size.</li>
 * <li>It can be create either a modal or non-modal dialog. In the case of a
 * non-modal dialog, the usual buttons (OK, Cancel, etc.) are suppressed.</li>
 * <li>It can optionally add a scroll bar around the provided {@link Component}
 * in case it is too large.</li>
 * <li>It can start with a particular {@link Component} having the keyboard
 * focus.</li>
 * </ul>
 * 
 * @author Curtis Rueden
 */
public class SwingDialog {

	/** Catch-all return value of {@link #show} if return value is non-integer. */
	public static final int UNKNOWN_OPTION = -0xdefea7;

	private final JOptionPane optionPane;

	/** Component housing confirmation buttons, hidden for non-modal dialogs. */
	private Component buttons;

	private Component parentComponent;

	private Component focusComponent;

	private String title;

	private boolean modal = true;

	private boolean resizable = true;

	private boolean sizeLimited = true;

	/**
	 * Creates a dialog box containing the given component.
	 * <p>
	 * By default, the dialog is <em>modal</em>, <em>resizable</em>, and
	 * <em>size limited</em>, with no parent component and no explicit focus
	 * component.
	 * </p>
	 * <p>
	 * If shown as a modal dialog, there will be a single OK button; otherwise, no
	 * buttons will be displayed.
	 * </p>
	 * 
	 * @param c the {@link Component} to display
	 * @param messageType the type of message to be displayed:
	 *          {@link JOptionPane#ERROR_MESSAGE},
	 *          {@link JOptionPane#INFORMATION_MESSAGE},
	 *          {@link JOptionPane#WARNING_MESSAGE},
	 *          {@link JOptionPane#QUESTION_MESSAGE}, or
	 *          {@link JOptionPane#PLAIN_MESSAGE}
	 * @param scrollBars whether to show scroll bars if the content is too large
	 *          to fit in the window.
	 */
	public SwingDialog(final Component c, final int messageType,
		final boolean scrollBars)
	{
		this(c, JOptionPane.DEFAULT_OPTION, messageType, scrollBars);
	}

	/**
	 * Creates a dialog box containing the given component.
	 * <p>
	 * By default, the dialog is <em>modal</em>, <em>resizable</em>, and
	 * <em>size limited</em>, with no parent component and no explicit focus
	 * component.
	 * </p>
	 * <p>
	 * If shown as a modal dialog, there will be buttons corresponding to the
	 * specified {@code optionType}; otherwise, no buttons will be displayed.
	 * </p>
	 * 
	 * @param c the {@link Component} to display
	 * @param optionType the options to display in the pane:
	 *          {@link JOptionPane#DEFAULT_OPTION},
	 *          {@link JOptionPane#YES_NO_OPTION},
	 *          {@link JOptionPane#YES_NO_CANCEL_OPTION}, or
	 *          {@link JOptionPane#OK_CANCEL_OPTION}
	 * @param messageType the type of message to be displayed:
	 *          {@link JOptionPane#ERROR_MESSAGE},
	 *          {@link JOptionPane#INFORMATION_MESSAGE},
	 *          {@link JOptionPane#WARNING_MESSAGE},
	 *          {@link JOptionPane#QUESTION_MESSAGE}, or
	 *          {@link JOptionPane#PLAIN_MESSAGE}
	 * @param scrollBars whether to show scroll bars if the content is too large
	 *          to fit in the window.
	 */
	public SwingDialog(final Component c, final int optionType,
		final int messageType, final boolean scrollBars)
	{
		optionPane = new JOptionPane(c, messageType, optionType);
		rebuildPane(scrollBars);
	}

	/** Gets the frame in which the dialog is displayed. */
	public Component getParent() {
		return parentComponent;
	}

	/**
	 * Sets the frame in which the dialog is displayed; if the
	 * <code>parentComponent</code> has no {@link java.awt.Frame}, a default one
	 * is used.
	 */
	public void setParent(final Component parentComponent) {
		this.parentComponent = parentComponent;
	}

	/** Gets the {@link Component} that will have the initial keyboard focus. */
	public Component getFocus() {
		return focusComponent;
	}

	/** Sets the {@link Component} that will have the initial keyboard focus. */
	public void setFocus(final Component focusComponent) {
		this.focusComponent = focusComponent;
	}

	/** Gets the title string that will be displayed in the dialog title bar. */
	public String getTitle() {
		return title;
	}

	/** Sets the title string to display in the dialog title bar. */
	public void setTitle(final String title) {
		this.title = title;
	}

	/** Gets whether the dialog is modal. */
	public boolean isModal() {
		return modal;
	}

	/**
	 * Specifies whether this dialog should be modal.
	 * <p>
	 * Note that non-modal dialogs do not show any buttons (e.g., OK or Cancel),
	 * regardless of the specified {@code optionType}, since the {@link #show}
	 * method does not block in that case.
	 * </p>
	 * 
	 * @param modal specifies whether dialog blocks input to other windows when
	 *          shown
	 */
	public void setModal(final boolean modal) {
		this.modal = modal;
		buttons.setVisible(modal);
	}

	/** Gets whether the dialog is resizable by the user. */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * Sets whether this dialog is resizable by the user.
	 * 
	 * @param resizable <code>true</code> if the user can resize this dialog;
	 *          <code>false</code> otherwise.
	 * @see java.awt.Dialog#isResizable
	 */
	public void setResizable(final boolean resizable) {
		this.resizable = resizable;
	}

	/** Gets whether the dialog's size is limited by the screen size. */
	public boolean isSizeLimited() {
		return sizeLimited;
	}

	/** Sets whether this dialog's size is limited by the screen size. */
	public void setSizeLimited(final boolean sizeLimited) {
		this.sizeLimited = sizeLimited;
	}

	/**
	 * Shows the dialog.
	 * 
	 * @return the option selected by the user. Depending on the dialog's
	 *         <code>optionType</code>, will be one of:
	 *         <ul>
	 *         <li>{@link JOptionPane#YES_OPTION}</li>
	 *         <li>{@link JOptionPane#NO_OPTION}</li>
	 *         <li>{@link JOptionPane#CANCEL_OPTION}</li>
	 *         <li>{@link JOptionPane#OK_OPTION}</li>
	 *         <li>{@link JOptionPane#CLOSED_OPTION}</li>
	 *         <li>{@link SwingDialog#UNKNOWN_OPTION}</li>
	 *         </ul>
	 *         If the dialog is non-modal, {@link JOptionPane#OK_OPTION} is always
	 *         returned, regardless of the specified {@code optionType}.
	 */
	public int show() {
		// create dialog, set properties, pack and show
		final JDialog dialog = optionPane.createDialog(parentComponent, title);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setResizable(resizable);
		dialog.setModal(modal);
		dialog.pack();

		if (sizeLimited) AWTWindows.ensureSizeReasonable(dialog);

		// HACK: When vertical scroll bar is needed, the dialog packs slightly too
		// small, resulting in an unnecessary horizontal scroll bar. Pad slightly.
		dialog.setSize(dialog.getSize().width + 20, dialog.getSize().height);

		AWTWindows.centerWindow(dialog);
		if (focusComponent != null) {
			setDefaultFocusComponent(dialog, focusComponent);
		}
		dialog.setVisible(true);

		// NB: surprisingly explicit dispose() required for modal dialogs to not
		// hang up program exit. and even more surprising that it's not needed for
		// modeless cases (like B&C). But testing bears this out.

		// clean up the dialog afterwards
		if (modal) dialog.dispose();

		if (!modal) return JOptionPane.OK_OPTION; // it's all good!

		// get result
		final Object result = optionPane.getValue();

		// return result
		if (result == null || (!(result instanceof Integer))) return UNKNOWN_OPTION;
		return (Integer) result;
	}

	/** Whether the dialog is currently being shown onscreen. */
	public boolean isVisible() {
		return optionPane.isVisible();
	}

	// -- Helper methods --

	private void rebuildPane(final boolean scrollBars) {
		final Component[] optionComponents = optionPane.getComponents();
		int messageIndex = 0, buttonIndex = optionComponents.length - 1;
		for (int i = 0; i < optionComponents.length; i++) {
			final String compName = optionComponents[i].getName();
			if ("OptionPane.messageArea".equals(compName)) messageIndex = i;
			else if ("OptionPane.buttonArea".equals(compName)) buttonIndex = i;
		}
		final Component mainPane = optionComponents[messageIndex];
		final Component buttonPane = optionComponents[buttonIndex];

		if (scrollBars) {
			// wrap main pane in a scroll pane
			final JScrollPane wrappedMainPane = new JScrollPane(mainPane);

			// HACK: On Mac OS X (and maybe other platforms), setting the button
			// pane's border directly results in the right inset of the EmptyBorder
			// not being respected. Nesting the button panel in another panel avoids
			// the problem.
			final JPanel wrappedButtonPane = new JPanel();
			wrappedButtonPane.setLayout(new BorderLayout());
			wrappedButtonPane.add(buttonPane);

			// fix component borders, so that scroll pane is flush with dialog edge
			final Border border = optionPane.getBorder();
			final Insets insets = border.getBorderInsets(optionPane);
			wrappedButtonPane.setBorder(new EmptyBorder(0, insets.left, insets.bottom,
				insets.right));
			optionPane.setBorder(null);

			// rebuild option pane with wrapped components
			optionPane.removeAll();
			for (int i = 0; i < optionComponents.length; i++) {
				if (i == messageIndex) optionPane.add(wrappedMainPane);
				else if (i == buttonIndex) optionPane.add(wrappedButtonPane);
				else optionPane.add(optionComponents[i]);
			}

			buttons = wrappedButtonPane;
		}
		else {
			buttons = buttonPane;
		}
	}

	/**
	 * Makes the given component grab the keyboard focus whenever the window gains
	 * the focus.
	 */
	private void setDefaultFocusComponent(final Window w, final Component c) {
		w.addWindowFocusListener(new WindowAdapter() {

			@Override
			public void windowGainedFocus(final WindowEvent e) {
				c.requestFocusInWindow();
			}

		});
	}

}
