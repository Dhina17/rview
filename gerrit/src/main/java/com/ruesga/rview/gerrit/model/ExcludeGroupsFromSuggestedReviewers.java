/*
 * Copyright (C) 2017 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview.gerrit.model;

/**
 * @link "https://gerrit-review.googlesource.com/Documentation/rest-api-changes.html#suggest-reviewers"
 */
@Deprecated
@SuppressWarnings("deprecation")
public final class ExcludeGroupsFromSuggestedReviewers {
    public static final ExcludeGroupsFromSuggestedReviewers INSTANCE
            = new ExcludeGroupsFromSuggestedReviewers();

    private ExcludeGroupsFromSuggestedReviewers() {
    }

    @Override
    public final String toString() {
        return "f";
    }
}