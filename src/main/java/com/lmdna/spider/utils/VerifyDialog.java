package com.lmdna.spider.utils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class VerifyDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField verifyField;
	private JLabel verifyLabel;
	private byte[] verifyImage;
	private CountDownLatch latch;

	private String verifyCode = "";

	/**
	 * @param verifyImage
	 * @param liteFetion
	 */
	public VerifyDialog(byte[] verifyImage) {
		this();
		this.verifyImage = verifyImage;
		this.latch = new CountDownLatch(1);
		this.verifyLabel.setIcon(new ImageIcon(verifyImage));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			VerifyDialog dialog = new VerifyDialog();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String waitOK() {
		try {
			this.latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return verifyCode;
	}

	/**
	 * Create the dialog.
	 */
	public VerifyDialog() {
		setTitle("验证码输入框");
		setBounds(100, 200, 198, 267);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel label = new JLabel("请输入验证码：");
			label.setBounds(12, 10, 127, 15);
			contentPanel.add(label);
		}

		verifyLabel = new JLabel("");
		verifyLabel.setBounds(22, 35, 117, 50);
		contentPanel.add(verifyLabel);

		verifyField = new JTextField();
		verifyField.setBounds(23, 94, 116, 21);
		contentPanel.add(verifyField);
		verifyField.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton flushButton = new JButton("刷新");
				flushButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// verifyImage =
						// liteFetion.retireVerifyImage(verifyImage.getVerifyType());
						verifyLabel.setIcon(new ImageIcon(verifyImage));
					}
				});
				flushButton.setActionCommand("Cancel");
				buttonPane.add(flushButton);
			}
			{
				JButton okButton = new JButton("确定");
				final VerifyDialog dialog = this;
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// verifyImage.setVerifyCode(verifyField.getText());
						verifyCode = verifyField.getText();
						latch.countDown();
						dialog.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
}
