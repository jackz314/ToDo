package com.jackz314.colorpicker.builder;

import com.jackz314.colorpicker.ColorPickerView;
import com.jackz314.colorpicker.renderer.ColorWheelRenderer;
import com.jackz314.colorpicker.renderer.FlowerColorWheelRenderer;
import com.jackz314.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
	public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
		switch (wheelType) {
			case CIRCLE:
				return new SimpleColorWheelRenderer();
			case FLOWER:
				return new FlowerColorWheelRenderer();
		}
		throw new IllegalArgumentException("wrong WHEEL_TYPE");
	}
}