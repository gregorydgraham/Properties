package nz.co.gregs.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Useful matchers for finding the correct properties.
 *
 * @author Malcolm Lett
 * @author Gregory Graham
 */
public class PropertyMatchers {

  /**
   * default constructor.
   *
   */
  public PropertyMatchers() {
  }
	

	/**
	 * Creates a regex matcher that avoids null pointer exceptions.
   * 
   * @param regex a regular expression
	 * @return a matcher that will use the regular expression to match strings after checking they're not null
	 */
  public static Matcher<String> matchesRegex(final String regex) {
		return new TypeSafeMatcher<String>() {
      @Override
			public void describeTo(Description description) {
				description.appendText("matches ").appendValue((regex==null?"[NULL]":regex));
			}

			@Override
			protected boolean matchesSafely(String item) {
				return (regex != null) && (item != null) && item.matches(regex);
			}
		};
	}
	
	/**
	 * Gets the first found item where there are many.
   * @param <E> a super class of all members 
	 * @param c a collection of items to check
	 * @param matcher the matcher to test the items with
	 * @return the first item to successfully match with the matcher
	 */
	public static <E> E firstItemOf(Collection<E> c, Matcher<? super E> matcher) {
		for (E item: c) {
			if (matcher.matches(item)) {
				return item;
			}
		}
		return null; // not found
	}
	
	/**
	 * Gets the zero or one item accepted by the matcher.
   * @param <E> a super class of all members 
	 * @param c a collection of items to check
	 * @param matcher the matcher to test the items with
	 * @return the item or null if not found
	 * @throws AssertionError if multiple items match
	 */
	public static <E> E itemOf(Collection<E> c, Matcher<? super E> matcher) {
		List<E> found = new ArrayList<E>();
		for (E item: c) {
			if (matcher.matches(item)) {
				found.add(item);
			}
		}
		if (found.size() > 1) {
			Description desc = new StringDescription();
			matcher.describeTo(desc);
			throw new AssertionError("Expected at most one item "+desc.toString()+", got "+found.size()+" items");
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		return null; // not found
	}
	
	/**
	 * Decorates another Matcher, retaining the behaviour but allowing tests
	 * to be slightly more expressive.
	 * <p>
	 * For example:  itemOf(collection, hasName(smelly))
	 *          vs.  itemOf(collection, that(hasName(smelly)))
   * @param <T> the class of the return type
   * @param matcher matcher to decorate
   * @return a new matcher containing the decorated matcher
	 */
	public static <T> Matcher<T> that(final Matcher<T> matcher) {
		return new BaseMatcher<T>() {

			@Override
			public boolean matches(Object item) {
				return matcher.matches(item);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("that ").appendDescriptionOf(matcher);
			}
			
		    @Override
		    public void describeMismatch(Object item, Description mismatchDescription) {
		        matcher.describeMismatch(item, mismatchDescription);
		    }
		};
	}
	
  /**
   * A matcher for finding properties based on the property name.
   *
   * @param name the property name to find
   * @return a matcher
   */
  public static Matcher<JavaProperty> hasJavaPropertyName(final String name) {
		return new TypeSafeDiagnosingMatcher<JavaProperty>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("has name ").appendValue(name);
			}

			@Override
			protected boolean matchesSafely(JavaProperty item, Description mismatchDescription) {
				if (!name.equals(item.name())) {
					mismatchDescription.appendText("has name ").appendValue(item.name());
					return false;
				}
				return true;
			}
		};
	}

	
  /**
   * A matcher for finding properties based on the whether it is a field property or bean property.
   *
   * @return a matcher
   */
	public static Matcher<JavaProperty> isJavaPropertyField() {
		return new TypeSafeDiagnosingMatcher<JavaProperty>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("is field ");
			}

			@Override
			protected boolean matchesSafely(JavaProperty item, Description mismatchDescription) {
				if (item.isField()) {
					mismatchDescription.appendText("is bean-property");
					return false;
				}
				return true;
			}
		};
	}
}
