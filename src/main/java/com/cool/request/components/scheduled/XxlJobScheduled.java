/*
 * Copyright 2024 XIN LIN HOU<hxl49508@gmail.com>
 * XxlJobScheduled.java is part of Cool Request
 *
 * License: GPL-3.0+
 *
 * Cool Request is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Request is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Request.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cool.request.components.scheduled;

import com.cool.request.common.bean.components.StaticComponent;
import com.cool.request.components.CanMark;
import com.cool.request.components.ComponentType;

import java.io.Serializable;

public class XxlJobScheduled extends BasicScheduled
        implements CanMark, Serializable, StaticComponent {
    private static final long serialVersionUID = 1000000000;

    @Override
    public ComponentType getComponentType() {
        return ComponentType.XXL_JOB;
    }
}
