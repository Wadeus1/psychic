package club.koupah.aue.gui.types.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import club.koupah.aue.Editor;
import club.koupah.aue.gui.GUIPanel;
import club.koupah.aue.gui.types.Setting;
import club.koupah.aue.gui.values.cosmetics.Cosmetic;
import club.koupah.aue.gui.values.cosmetics.Cosmetic.CosmeticType;
import club.koupah.aue.gui.values.cosmetics.Hats;
import club.koupah.aue.utility.ImageUtil;
import club.koupah.aue.utility.PopUp;

public class MultiSetting extends Setting {

	List<String> values;
	boolean keepCurrent;

	boolean hasPreview = false;

	JLabel imageLabel;

	int[] imageSettings;

	boolean customOffsets;

	int[] bounds;

	int[] currentBounds;

	int[] cosmeticOffset;

	CosmeticType cosmeticType;

	public MultiSetting(JLabel label, JComboBox<String> component, List<String> values, boolean addKeepCurrent,
			boolean imagePreview, int[] offset, CosmeticType ct, int settingIndex) {
		this(label, component, values, null, addKeepCurrent, settingIndex);

		this.cosmeticType = ct;
		this.hasPreview = imagePreview;
		if (hasPreview) {
			ComboBoxRenderer renderer = new ComboBoxRenderer();

			component.setRenderer(renderer);
		}
		this.imageLabel = new JLabel();

		// Per instance offsets for displaying the image & the width/height of the image
		this.imageSettings = offset;

		// Only hats are currently supporting custom offsets, so easy hard coded check
		customOffsets = label.getText().split(":")[0].equals("Hat");
		cosmeticOffset = new int[] { 0, 0, 0, 0 };
	}

	public MultiSetting(JLabel label, JComboBox<String> component, List<String> values, boolean addKeepCurrent,
			int settingIndex) {
		this(label, component, values, null, addKeepCurrent, settingIndex);
	}

	List<Integer> saveValues;

	public MultiSetting(JLabel label, JComboBox<String> component, List<String> values, List<Integer> saveValues,
			boolean addKeepCurrent, int settingIndex) {
		super(label, component, settingIndex);
		this.keepCurrent = addKeepCurrent;
		this.values = values;

		if (saveValues != null) {
			this.saveValues = saveValues;
		}

		// Updated to show more items to make it easier to choose
		component.setMaximumRowCount(14);

		updateValues(values);

		component.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Separate function to allow for future overrides
				settingChanged(arg0);
			}
		});

	}

	public int getSelectedIndex() {
		return this.values.indexOf(((JComboBox<String>) component).getSelectedItem());
	}

	public String getIndex(int index) {
		return this.values.get(index);
	}

	@Override
	public void addToPane(GUIPanel contentPane) {
		super.addToPane(contentPane);

		if (hasPreview) {

			int width = 60; // This is really just the JLabel width, not image

			this.bounds = new int[] { component.getX() - width + imageSettings[0],
					component.getY() + imageSettings[1] + 10 - (width / 2), width + imageSettings[2],
					width + imageSettings[3] };

			this.currentBounds = this.bounds.clone();

			this.imageLabel.setBounds(currentBounds[0], currentBounds[1], currentBounds[2], currentBounds[3]);

			contentPane.add(this.imageLabel, imageSettings == null ? -1 : imageSettings[4]);
		}
	}

	@Override
	public void updateLabel() {
		label.setText(getLabelText() + getCurrentSettingValue());
	}

	public void settingChanged(ActionEvent arg0) {
		if (hasPreview) {

			if (customOffsets) {
				cosmeticOffset = Hats.getOffsetByID(this.getComponentValue(false));
				this.currentBounds = this.bounds.clone();
			}

			this.imageLabel.setBounds(currentBounds[0] + cosmeticOffset[0], currentBounds[1] + cosmeticOffset[1],
					currentBounds[2] + cosmeticOffset[2], currentBounds[3] + cosmeticOffset[3]);

			this.imageLabel.setIcon(ImageUtil.getIcon(Cosmetic.class, getCosmeticImagePath(this.getComponentValue(false)),
					50 + imageSettings[2] + cosmeticOffset[2], 40 + imageSettings[3] + cosmeticOffset[3]));

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateComponent() {
		((JComboBox<String>) component).setSelectedItem(getCurrentSettingValue());
	}

	private String getCosmeticImagePath(String cosmeticID) {
		return "images/" // Images package
				+ this.labelText.split(":")[0].toLowerCase() // Returns hat, pet, skin depending on the type. This is
				// just an easy way to make this modular
				+ "/" + cosmeticID + ".png"; // cosmeticID is easier to work with then naming the pictures proper names,
		// and PNG is just so we have transparency (And source images are PNG
		// :P)
	}

	/*
	 * Super messy fix, 24/07/2021 It seems like Among Us changed how some settings
	 * saved, ugh
	 */

	@Override
	public String getProperValue() {
//		final String saveValue = String.valueOf(((JComboBox<String>) component).getSelectedIndex());
//
//		if (saveValue.equals("ErrorInSaveValue")) {
//			System.out.println("Error in save value for: " + label.getText());
//			new PopUp("Error in save value for " + label.getText().split(":")[0]); // I really need to add a function so
//		} // I don't have to split everytime
//			// lol
//
//		return saveValue;

		int selected = ((JComboBox<String>) component).getSelectedIndex();

		if (this.saveValues == null || selected + 1 > this.saveValues.size()) {
			return String.valueOf(selected);
		} else return String.valueOf(this.saveValues.get(selected));
	}

	@Override
	protected String getCurrentSettingValue() {
		if (this.saveValues != null && this.saveValues.indexOf(Integer.parseInt(currentSettings[settingIndex])) != -1) {
			int index = this.saveValues.indexOf(Integer.parseInt(currentSettings[settingIndex]));
			return String.valueOf(((JComboBox<String>) component).getItemAt(index));
		}
		return String.valueOf(((JComboBox<String>) component).getItemAt(Integer.parseInt(currentSettings[settingIndex])));

	}

	@Override
	public String getComponentValue(boolean fromLabel) {
		return String.valueOf(((JComboBox<String>) component).getSelectedIndex());
	}

	public void setValues(List<String> items) {
		updateValues(items);
	}

	public void forceValues(List<String> values) {
		JComboBox<String> component = ((JComboBox<String>) this.component);
		component.removeAllItems();
		for (String value : values) {
			component.addItem(value);
		}
		component.setSelectedIndex(0);
	}

	public void updateValues(List<String> values) {
		@SuppressWarnings("unchecked")
		JComboBox<String> component = ((JComboBox<String>) this.component);
		component.removeAllItems();

		boolean currentRemoved = Editor.getInstance().isVisible() ? !values.contains(getCurrentSettingValue())
				: keepCurrent;

		// Essentially, if it's cosmetic then add keep current
		if (keepCurrent)
			Collections.sort(values);

		if (keepCurrent || values.size() == 0 || currentRemoved) {
			component.insertItemAt("Keep Current", 0);
			component.setSelectedIndex(0);
		}

		for (String value : values) {
			if (value.equals("None")) {
				component.addItem("TEMP-ITEM");
				component.insertItemAt(value, 1);
			} else component.addItem(value);
		}

		component.removeItem("TEMP-ITEM");

	}

	public void originalValues() {
		updateValues(values);
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {

		private static final long serialVersionUID = 1L;

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			for (String value : MultiSetting.this.values) {
				String ID = Cosmetic.getIDbyName(MultiSetting.this.cosmeticType, value);
				if (!ID.equals("ErrorFinding")) {
					try {
						images.put(value, new ImageIcon(ImageUtil
								.scaleProper(ImageUtil.getImage(Cosmetic.class, getCosmeticImagePath(ID)), 30, 30, true)));
					} catch (IllegalArgumentException e) {
//						This is never thrown, but I'll fix that another time
						JOptionPane.showMessageDialog(null, String.format("Failed to get the image for \"%s\"", ID));
					}
				}
			}
		}

		HashMap<String, ImageIcon> images = new HashMap<String, ImageIcon>();

		/*
		 * This method finds the image and text corresponding to the selected value and
		 * returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
//Get the selected index. (The index param isn't
//always valid, so just use the value.)

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if (value == null) {
				list.remove(this);
				setVisible(false);
				return this;
			}

			ImageIcon icon = images.get(value);

			setIcon(icon);

			setText(value.toString());
			setFont(list.getFont());

			return this;
		}
	}
}
