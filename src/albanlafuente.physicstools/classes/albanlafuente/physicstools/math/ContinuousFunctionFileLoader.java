/*
 * Copyright (C) 2021 audreyazura
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package albanlafuente.physicstools.math;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;
import albanlafuente.physicstools.physics.PhysicsVariables;

/**
 *
 * @author audreyazura
 */
public interface ContinuousFunctionFileLoader
{
    /**
     * method to overwrite to write your specific call to the ContinuousFunction constructor
     * @param p_functionFile the file containing the function values
     * @param p_abscissaScale the unit of the abscissa
     * @param p_ordinateScale the unit of the ordinate
     * @return 
     */
    public ContinuousFunction loadFunction (File p_functionFile, PhysicsVariables.UnitsPrefix p_abscissaUnit, PhysicsVariables.UnitsPrefix p_ordinateunit) throws DataFormatException, IOException, ArrayIndexOutOfBoundsException;
}
