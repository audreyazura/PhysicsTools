/*
 * Copyright (C) 2022 audreyazura
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

import java.math.BigDecimal;
import java.math.MathContext;
import org.nevec.rjm.BigDecimalMath;

/**
 *
 * @author audreyazura
 */
public class Big2DVector
{
    private final BigDecimal m_x;
    private final BigDecimal m_y;
    
    public Big2DVector ()
    {
        m_x = BigDecimal.ZERO;
        m_y = BigDecimal.ZERO;
    }
    
    public Big2DVector (BigDecimal p_x, BigDecimal p_y)
    {
        m_x = new BigDecimal(p_x.toString());
        m_y = new BigDecimal(p_y.toString());
    }
    
    public Big2DVector (Big2DVector p_vector)
    {
        m_x = p_vector.getX();
        m_y = p_vector.getY();
    }
    
    public Big2DVector add(Big2DVector vectorToAdd)
    {
        return new Big2DVector(m_x.add(vectorToAdd.getX()), m_y.add(vectorToAdd.getY()));
    }
    
    public BigDecimal dotProduct(Big2DVector vectorToMultiply)
    {
        BigDecimal x1x2 = m_x.multiply(vectorToMultiply.getX());
        BigDecimal y1y2 = m_y.multiply(vectorToMultiply.getY());
        
        return x1x2.add(y1y2);
    }
    
    public BigDecimal norm()
    {
        BigDecimal selfDotProduct = this.dotProduct(this);
        
        BigDecimal norm;
        if (selfDotProduct.compareTo(BigDecimal.ZERO) == 0)
        {
            norm = BigDecimal.ZERO;
        }
        else
        {
            norm = BigDecimalMath.sqrt(selfDotProduct, MathContext.DECIMAL128);
        }
        
        return norm;
    }
    
    public BigDecimal getX()
    {
        return new BigDecimal(m_x.toString());
    }
    
    public BigDecimal getY()
    {
        return new BigDecimal(m_y.toString());
    }
    
    public boolean isCodirectionnal(Big2DVector p_vector)
    {
        BigDecimal xRatio = m_x.divide(p_vector.getX(), MathContext.DECIMAL128);
        BigDecimal yRatio = m_y.divide(p_vector.getY(), MathContext.DECIMAL128);
        
        return xRatio.compareTo(yRatio) == 0;
    }
    
    public Big2DVector rotate(double angle)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        BigDecimal rotatedX = (m_x.multiply(new BigDecimal(cos))).subtract(m_y.multiply(new BigDecimal(sin)));
        BigDecimal rotatedY = (m_x.multiply(new BigDecimal(sin))).add(m_y.multiply(new BigDecimal(cos)));
        
        return new Big2DVector(rotatedX, rotatedY);
    }
    
    public Big2DVector scale(BigDecimal multiplier)
    {
        return new Big2DVector(m_x.multiply(multiplier), m_y.multiply(multiplier));
    }
    
    @Override
    public String toString()
    {
        return "(" + m_x.toString() + "; " + m_y.toString() + ")";
    }
}
