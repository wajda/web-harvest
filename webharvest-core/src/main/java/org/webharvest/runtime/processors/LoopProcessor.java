/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.processors;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.math.NumberUtils;
import org.webharvest.WHConstants;
import org.webharvest.definition.LoopDef;
import org.webharvest.definition.ProcessorElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loop list processor.
 */
public class LoopProcessor extends AbstractProcessor<LoopDef> {

    public LoopProcessor(LoopDef loopDef) {
        super(loopDef);
    }

    public Variable execute(final Scraper scraper, final DynamicScopeContext context) throws InterruptedException {
        final String item = BaseTemplater.evaluateToString(elementDef.getItem(), null, scraper);
        final String index = BaseTemplater.evaluateToString(elementDef.getIndex(), null, scraper);
        final String maxLoopsString = BaseTemplater.evaluateToString(elementDef.getMaxloops(), null, scraper);
        final String filter = BaseTemplater.evaluateToString(elementDef.getFilter(), null, scraper);
        final boolean isEmpty = CommonUtil.getBooleanValue(BaseTemplater.evaluateToString(elementDef.getEmpty(), null, scraper), false);

        this.setProperty("Item", item);
        this.setProperty("Index", index);
        this.setProperty("Max Loops", maxLoopsString);
        this.setProperty("Filter", filter);
        this.setProperty("Empty", String.valueOf(isEmpty));

        ProcessorElementDef loopValueDef = elementDef.getLoopValueDef();
        Variable loopValue = new BodyProcessor(loopValueDef).run(scraper, context);
        debug(loopValueDef, scraper, loopValue);


        final Iterator iter = loopValue != null ? loopValue.toIterator() : null;

        if (iter == null) {
            return EmptyVariable.INSTANCE;

        } else {
            final List<Variable> resultList = new ArrayList<Variable>();
            final Iterator filteredIterator = filter != null ? createFilteredList(iter, filter) : iter;

            final double maxLoops = NumberUtils.toDouble(maxLoopsString, WHConstants.DEFAULT_MAX_LOOPS);
            for (int i = 1; filteredIterator.hasNext() && i <= maxLoops; i++) {
                Variable currElement = (Variable) filteredIterator.next();

                // define current value of item variable
                if (item != null && !"".equals(item)) {
                    context.setLocalVar(item, currElement);
                }

                // define current value of index variable
                if (index != null && !"".equals(index)) {
                    context.setLocalVar(index, new NodeVariable(String.valueOf(i)));
                }

                // execute the loop body
                ProcessorElementDef bodyDef = elementDef.getLoopBodyDef();
                Variable loopResult = (bodyDef != null) ? new BodyProcessor(bodyDef).run(scraper, context) : EmptyVariable.INSTANCE;
                debug(bodyDef, scraper, loopResult);
                if (!isEmpty) {
                    resultList.addAll(loopResult.toList());
                }
            }
            return isEmpty ? EmptyVariable.INSTANCE : new ListVariable(resultList);

        }
    }

    /**
     * Create filtered list based on specified list and filterStr
     *
     * @param iter
     * @param filterStr
     * @return Filtered list
     */
    private Iterator createFilteredList(Iterator iter, String filterStr) {
        final Filter filter = new Filter(filterStr);

        return IteratorUtils.filteredIterator(iter, new Predicate() {
            final Set<String> stringSet = new HashSet<String>();
            int index = 1;
            @Override public boolean evaluate(Object curr) {
                try {
                    if (filter.isInFilter(index)) {
                        if (filter.isUnique) {
                            final String currStr = curr.toString();
                            if (!stringSet.contains(currStr)) {
                                stringSet.add(currStr);
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                    return false;
                } finally {
                    index++;
                }
            }
        });
    }

    /**
     * x - starting index in range
     * y - ending index in range
     */
    private static class IntRange extends CommonUtil.IntPair {

        // checks if strings is in form [n][-][m]

        static boolean isValid(String s) {
            Pattern pattern = Pattern.compile("(\\d*)(-?)(\\d*?)");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        }

        public IntRange(String s, int size) {
            defineFromString(s, '-', size);
        }

        public boolean isInRange(int index) {
            return index >= x && index <= y;
        }

    }

    /**
     * x - starting index
     * y - index skip - x is first, x+y second, x+2y third, end so on.
     */
    private static class IntSublist extends CommonUtil.IntPair {

        // checks if strings is in form [n][:][m]

        static boolean isValid(String s) {
            Pattern pattern = Pattern.compile("(\\d*)(:?)(\\d*?)");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        }

        private IntSublist(int x, int y) {
            super(x, y);
        }

        public IntSublist(String s, int size) {
            defineFromString(s, ':', size);
        }


        public boolean isInSublist(int index) {
            return (index - x) % y == 0;
        }

    }

    /**
     * Class that represents filter for list filtering. It is created based on filter string.
     * Filter string is comma separated list of filter tokens. Valid filter tokens are:
     * m - specific integer m
     * m-n - integers in specified range, if m is ommited it's vaue is 1, if n is
     * ommited it's value is specified size of list to be filtered
     * m:n - all integerers starting from m and all subsequent with step n,
     * m, m+1*n , m+2*n, ...
     * odd - the same as 1:2
     * even - the same as 2:2
     * unique - tells that list must contain unique values (no duplicates)
     */
    private static class Filter {

        private boolean isUnique = false;
        private List<CommonUtil.IntPair> filterList;

        private Filter(String filterStr) {
            StringTokenizer tokenizer = new StringTokenizer(filterStr, ",");
            filterList = new ArrayList<CommonUtil.IntPair>();

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();

                if ("unique".equals(token)) {
                    isUnique = true;
                } else if ("odd".equals(token)) {
                    filterList.add(new IntSublist(1, 2));
                } else if ("even".equals(token)) {
                    filterList.add(new IntSublist(2, 2));
                } else if (IntRange.isValid(token)) {
                    filterList.add(new IntRange(token, Integer.MAX_VALUE));
                } else if (IntSublist.isValid(token)) {
                    filterList.add(new IntSublist(token, Integer.MAX_VALUE));
                }
            }
        }

        /**
         * Checks if specified integer passes the filter
         */
        private boolean isInFilter(int num) {
            if (filterList.size() == 0) {
                return true;
            }

            for (CommonUtil.IntPair curr : filterList) {
                if (curr instanceof IntRange && ((IntRange) curr).isInRange(num)
                        || curr instanceof IntSublist && ((IntSublist) curr).isInSublist(num)) {
                    return true;
                }
            }

            return false;
        }

    }

}