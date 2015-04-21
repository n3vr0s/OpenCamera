/*
 * Copyright 2002-2012 Drew Noakes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    http://drewnoakes.com/code/exif/
 *    http://code.google.com/p/metadata-extractor/
 */
package com.almalence.util.exifreader.imaging;

/**
 * Contains helper methods that perform photographic conversions.
 *
 * @author Drew Noakes http://drewnoakes.com
 */
public final class PhotographicConversions
{
    public static final double ROOT_TWO = Math.sqrt(2);

    private PhotographicConversions() throws Exception
    {
        throw new Exception("Not intended for instantiation.");
    }

    /**
     * Converts an aperture value to its corresponding F-stop number.
     *
     * @param aperture the aperture value to convert
     * @return the F-stop number of the specified aperture
     */
    public static double apertureToFStop(double aperture)
    {
        return Math.pow(ROOT_TWO, aperture);
    }

}