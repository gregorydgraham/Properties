package nz.co.gregs.properties;



import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.co.gregs.properties.InterfaceInfo;
import nz.co.gregs.properties.PropertyWrapperDefinition;
import nz.co.gregs.properties.adapt.AdaptableType;
import nz.co.gregs.properties.exceptions.UnsupportedType;

import org.junit.Test;

public class InterfaceInfoTest {
	@Test
	public void recognisesImplementationGivenStandardInputs() {
		InterfaceInfo info = new InterfaceInfo(MyInterface.class,
				SimpleIntegerMyIntegerImpl.class);
		assertThat(info.isInterfaceImplementedByImplementation(), is(true));
	}
	
	@Test
	public void getsBoundsGivenDirectImplementationUsingClassTypeArguments() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class, SimpleIntegerMyIntegerImpl.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Integer.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyInteger.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsBoundsGivenDirectImplementationUsingTypeVariableArguments() {
		class GenericIntegerDBIntegerImpl<A extends Number, B extends MyNumber> implements MyInterface<A, B> {
			public A toExternalValue(B dbvValue) {
				return null;
			}
			public B toInternalValue(A objectValue) {
				return null;
			}
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				GenericIntegerDBIntegerImpl.class);
		System.out.println(Arrays.toString(bounds));
		
		assertThat(bounds[0].upperType(), is((Object)Number.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyNumber.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsBoundsGivenDirectImplementationUsingClassTypeArgumentsAndExtraneousMethods() {
		class MyIntegerDBIntegerAdaptorWithNumberDBNumberMethods implements MyInterface<Integer, MyInteger> {
			public Integer toExternalValue(MyInteger dbvValue) {
				throw new UnsupportedOperationException("toObjectValue(DBInteger): Integer");
			}
			public Number toExternalValue(MyNumber dbvValue) {
				throw new UnsupportedOperationException("toObjectValue(DBNumber): Number");
			}
			public MyInteger toInternalValue(Integer objectValue) {
				throw new UnsupportedOperationException("toDBvValue(Integer): DBInteger");
			}
			public MyInteger toInternalValue(Number objectValue) {
				throw new UnsupportedOperationException("toDBvValue(Number): DBNumber");
			}
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				MyIntegerDBIntegerAdaptorWithNumberDBNumberMethods.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Integer.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyInteger.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsBoundsGivenIndirectImplementationUsingClassTypeArguments() {
		class ConcretePartialImplementationOfConcreteType
				extends AbstractPartialImplementationWithConcreteType {
			public MyNumber toInternalValue(Number objectValue) {
				return null;
			}
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				ConcretePartialImplementationOfConcreteType.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Number.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyNumber.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsBoundsGivenIndirectImplementationUsingConcretizedWildcardTypeArguments() {
		class ConcretePartialImplementationOfWildcardType
				extends AbstractPartialImplementationWithWildcardType<Integer, MyInteger> {
			public MyInteger toInternalValue(Integer objectValue) {
				return null;
			}
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				ConcretePartialImplementationOfWildcardType.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Integer.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyInteger.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsBoundsGivenDoublyIndirectImplementationAndScatteredClassTypeArguments() {
		abstract class AbstractPartialReImplementationOfWildcardTypeWithWildcardType<I extends Integer>
				extends AbstractPartialImplementationWithWildcardType<I, MyInteger> {
			public MyInteger toInternalValue(I objectValue) {
				return null;
			}
		}
		
		class ConcretePartialReImplementationOfWildcardTypeWithWildcardType
				extends AbstractPartialReImplementationOfWildcardTypeWithWildcardType<Integer> {
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				ConcretePartialReImplementationOfWildcardTypeWithWildcardType.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Integer.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)MyInteger.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsDefaultBoundsGivenItself() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class, MyInterface.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Object.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)AdaptableType.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}

	@Test
	public void getsDefaultBoundsGivenOnClassWhenUsingConvenienceMethod() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Object.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)AdaptableType.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsDefaultBoundsOnObjectClass() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(Object.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds.length, is(0));
	}

	@Test
	public void getsDefaultBoundsOnClassClass() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(Class.class);
		System.out.println(Arrays.toString(bounds));
		
		assertThat(bounds[0].upperType(), is((Object)Object.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
	}

	@Test
	public void getsDefaultBoundsOnEnumClass() throws UnsupportedType {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(Enum.class);
		System.out.println(Arrays.toString(bounds));
		
		assertThat(bounds[0].upperClass(), is((Object)Enum.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
	}
	
	@Test
	public void getsNullBoundsGivenNonImplementation() {
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class, Class.class);
		assertThat(bounds, is(nullValue()));
	}

        @SuppressWarnings("rawtypes")
	@Test
	public void getsDefaultBoundsGivenNonSpecifiedTypeArguments() {
			InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MyInterface.class,
				new MyInterface(){
                                        @Override
					public Object toExternalValue(AdaptableType dbvValue) {
						return null;
					}
                                        @Override
					public AdaptableType toInternalValue(Object objectValue) {
						return null;
					}
			}.getClass());
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperType(), is((Object)Object.class));
		assertThat(bounds[0].lowerType(), is(nullValue()));
		
		assertThat(bounds[1].upperType(), is((Object)AdaptableType.class));
		assertThat(bounds[1].lowerType(), is(nullValue()));
	}

	@Test
	public void getsBoundsGivenInterfaceTypeWithMultipleBoundingTypes() throws UnsupportedType {
		class MultiBoundedInterface<T extends Serializable & Map<?,?>> {}
		
		class MySubclass<P extends Serializable & Map<?,?>> extends MultiBoundedInterface<P> {}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(MultiBoundedInterface.class, MySubclass.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(Arrays.asList(bounds[0].upperClasses()), contains((Object)Serializable.class, (Object)Map.class));
		assertThat(bounds[0].lowerTypes(), is(nullValue()));
	}

	@Test
	public void getsBoundsGivenInterfaceTypeWithSimpleParameterizedArgument() throws UnsupportedType {
		class ParamaterizedArgumentInterface<T extends Map<?,?>> {
		}
		
		class MySubclass extends ParamaterizedArgumentInterface<HashMap<Object, Number>> {
		}
		
		InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(ParamaterizedArgumentInterface.class,
				MySubclass.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperClass(), is((Object)HashMap.class));
		assertThat(bounds[0].lowerClass(), is(nullValue()));
	}

        @SuppressWarnings("rawtypes")
	@Test
	public void getsBoundsGivenInterfaceTypeWithSimpleParameterizedArgument2() throws UnsupportedType {
		class ParamaterizedArgumentInterface<T extends Map<?,?>> {
		}
		
		class MySubclass extends ParamaterizedArgumentInterface {
		}
		
			InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(ParamaterizedArgumentInterface.class,
				MySubclass.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperClass(), is((Object)Map.class));
		assertThat(bounds[0].lowerClass(), is(nullValue()));
	}

        @SuppressWarnings("rawtypes")
	@Test
	public void getsBoundsGivenInterfaceTypeWithRecursiveParameterizedArgument() throws UnsupportedType {
		class ParamaterizedArgumentInterface<E extends Enum<E>> {
		}
		
		class MySubclass extends ParamaterizedArgumentInterface {
		}
		
			InterfaceInfo.ParameterBounds[] bounds = InterfaceInfo.getParameterBounds(ParamaterizedArgumentInterface.class,
				MySubclass.class);
		System.out.println(Arrays.toString(bounds));
		assertThat(bounds[0].upperClass(), is((Object)Enum.class));
		assertThat(bounds[0].lowerClass(), is(nullValue()));
	}
	
	@Test
	public void quietlyRecognisesNotImplementation() {
		InterfaceInfo info = new InterfaceInfo(List.class, SimpleIntegerMyIntegerImpl.class);
		assertThat(info.isInterfaceImplementedByImplementation(), is(false));
	}

	@Test
	public void nullWhenInterfaceNotAnInterfaceAndUnrelated() {
		InterfaceInfo info = new InterfaceInfo(ArrayList.class, SimpleIntegerMyIntegerImpl.class);
		assertThat(info.getInterfaceParameterValueBounds(), is(nullValue()));
	}

	@Test
	public void acceptsWhenImplIsAbstract() throws UnsupportedType {
		InterfaceInfo info = new InterfaceInfo(MyInterface.class, AbstractPartialImplementationWithWildcardType.class);
		assertThat(info.getInterfaceParameterValueBounds(), is(not(nullValue())));
		System.out.println(Arrays.toString(info.getInterfaceParameterValueBounds()));
		assertThat(info.getInterfaceParameterValueBounds().length, is(2));
		assertThat(info.getInterfaceParameterValueBounds()[0].upperClass(), is((Object) Number.class));
		assertThat(info.getInterfaceParameterValueBounds()[1].upperClass(), is((Object) AdaptableType.class));
	}

	@Test
	public void nullWhenImplIsUnrelatedInterface() throws UnsupportedType {
		InterfaceInfo info = new InterfaceInfo(MyInterface.class, List.class);
		assertThat(info.getInterfaceParameterValueBounds(), is(nullValue()));
	}

	@Test
	public void acceptsAnonymousImpl() {
            InterfaceInfo interfaceInfo = new InterfaceInfo(MyInterface.class, new MyInterface<Object,AdaptableType>(){
                                                  @Override
                                                  public Object toExternalValue(AdaptableType dbvValue) {
                                                          return null;
                                                  }

                                                  @Override
                                                  public AdaptableType toInternalValue(Object objectValue) {
                                                          return null;
                                                  }}.getClass());
	}
	
	public interface MyInterface<T, Q extends AdaptableType> {
		public T toExternalValue(Q dbvValue);

		public Q toInternalValue(T objectValue);
	}	
	
	
	
	public static class SimpleIntegerMyIntegerImpl implements MyInterface<Integer, MyInteger> {
                @Override
		public Integer toExternalValue(MyInteger dbvValue) {
			return null;
		}

		@Override
		public MyInteger toInternalValue(Integer objectValue) {
			return null;
		}
	}

	
	public abstract class AbstractPartialImplementationWithConcreteType implements MyInterface<Number, MyNumber> {
		@Override
		public Number toExternalValue(MyNumber dbvValue) {
			return null;
		}
	}

	public abstract class AbstractPartialImplementationWithWildcardType<T extends Number, Q extends AdaptableType> implements MyInterface<T, Q> {
		@Override
		public T toExternalValue(Q dbvValue) {
			return null;
		}
	}
}
