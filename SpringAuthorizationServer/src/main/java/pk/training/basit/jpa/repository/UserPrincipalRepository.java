package pk.training.basit.jpa.repository;

import org.springframework.data.repository.CrudRepository;

import pk.training.basit.jpa.entity.UserPrincipal;

/**
 * Spring Data, a Spring project separate from but dependent on Spring Framework, can write your repositories for you. All you have 
 * to do is create an interface and Spring Data dynamically generates the necessary code to implement that interface at run time.
 * 
 * When you use Spring Data, that repository code gets written for you. There are very few cases when you would ever need to write 
 * repository code that Spring Data couldn’t handle automatically, and facilities exist to handle those rare occasions.
 * 
 * Spring Data supports a variety of data access methodologies, including JPA, JdbcTemplate, NoSQL, and more. Its primary subproject,
 * Spring Data Commons, provides a core toolset that all other subprojects use to create repositories. The Spring Data JPA subproject
 * provides support for repositories implemented against the Java Persistence API.
 * 
 * One of the tools supplied by Spring Data Commons is the org.springframework.data.repository.Repository<T, ID extends Serializable>
 * interface. All Spring Data repository interfaces must extend this marker interface, which specifies no methods. Only interfaces 
 * extending Repository are eligible for dynamic implementation. The generic type parameters T and ID capture the entity type and 
 * identifier type, respectively.
 * 
 * You may create an interface that extends Repository directly, but because it specifies no methods, you will probably never do this.
 * A more useful approach is to extend org.springframework.data.repository.CrudRepository<T, ID>, which specifies numerous methods 
 * for basic CRUD operations.
 * 
 * 	-- count() returns a long representing the total number of unfiltered entities extending T.
 	-- delete(T) and delete(ID) delete the single, specified entity, whereas delete(Iterable<? extends T>) deletes multiple entities
 	   and deleteAll() deletes every entity of that type.
 
 	-- exists(ID) returns a boolean indicating whether the entity of this type with the given surrogate key exists.
 	-- findAll() returns all entities of type T, whereas findAll(Iterable<ID>) returns the entities of type T with the given 
 	   surrogate keys. Both return Iterable<T>.
 
 	-- findOne(ID) retrieves a single entity of type T given its surrogate key.
 	-- save(S) saves the given entity (insert or update) of type S where S extends T, and returns S, the saved entity.
 	-- save(Iterable<S>) saves all the entities (again, S extends T) and returns the saved entities as a new Iterable<S>.
 * 
 * All Spring Data projects already know how to implement all these methods for a given type. You’ll notice, however, that this 
 * repository still doesn’t specify methods that support paging and sorting. This is so that these methods don’t clutter any 
 * repositories that you don’t want to support paging and sorting. If you want a repository to provide paging and sorting methods, 
 * its interface can extend org.springframework.data.repository.PagingAndSortingRepository<T, ID extends Serializable>.

	-- findAll(Sort) returns all T entities as an Iterable<T> sorted with the provided Sort instructions.
	-- findAll(Pageable) returns a single org.springframework.data.domain.Page<T> of entities sorted and bounded with the provided 
	   Pageable instructions.
 * 
 * An org.springframework.data.domain.Sort object encapsulates information about the properties that should be used to sort a result
 * set and in what direction they should be sorted. An org.springframework.data.domain.Pageable encapsulates a Sort as well as the 
 * number of entities per page and which page to return (both ints). In a web application, you don’t usually have to worry about 
 * creating Pageable objects on your own. Spring Data provides two 
 * org.springframework.web.method.support.HandlerMethodArgumentResolver implementations that can turn HTTP request parameters into 
 * Pageable and Sort objects, respectively: org.springframework.data.web.PageableHandlerMethodArgumentResolver and 
 * org.springframework.data.web.SortHandlerMethodArgumentResolver.
 * 
 * All these predefined methods are helpful, and the standardized Sort and Pageable objects definitely come in handy, but you still 
 * have no way to find specific entities or lists of entities using anything other than surrogate keys — at least, not without 
 * creating your own method implementations. This is where Spring Data’s query methods come in to play.
 * 
 * You probably noticed that the JPA repository interfaces you created extended CrudRepository from Spring Data Commons instead of 
 * JpaRepository from Spring Data JPA. You should only extend the interface whose methods you want to expose to your services. In 
 * most cases you shouldn’t need to expose the fact that your repositories use JPA, and so you shouldn’t extend JpaRepository. If you
 * want to expose pagination capabilities, extend PagingAndSortingRepository. Only if you need to expose JPA-specific behavior, such
 * as batch deletions or EntityManager flushing, should you extend JpaRepository.
 * 
 * @author basit.ahmed
 *
 */
public interface UserPrincipalRepository extends CrudRepository<UserPrincipal, Long> {
    
	/**
	 * Query methods are specially defined methods that tell Spring Data how to find entities. The name of a query method starts with
	 * find...By, get...By, or read...By and is followed by the names of properties that should be matched on. The method parameters
	 * provide the values that should match the properties specified in the method name (in the same order the properties are 
	 * listed in the method name if the values are of the same type). The method return type tells Spring Data whether to expect a 
	 * single result (T) or multiple results (Iterable<T>, List<T>, Collection<T>, Page<T>, and so on). So, for example, in a 
	 * BookRepository you might need to locate a book by its ISBN, author, or publisher:
	 * 
	 * 		public interface BookRepository extends PagingAndSortingRepository<Book, Long> {
				Book findByIsbn(String isbn);
				List<Book> findByAuthor(String author);
				List<Book> findByPublisher(String publisher);
			}
	 * 
	 * The algorithm that analyzes these methods knows that findByIsbn should match the Book’s isbn property to the method parameter 
	 * and that the result should be unique. Likewise, it knows that findByAuthor and findByPublisher should match multiple records 
	 * using the author and publisher Book properties, respectively. Notice that the property names referenced in the repository 
	 * method names match the JPA property names of the Book entity — this is the convention that you must follow. In most cases, 
	 * this is also the JavaBean property names. Of course, an author can write many books and a publisher most certainly publishes 
	 * many, so you probably need your query methods to support pagination.

			public interface BookRepository extends PagingAndSortingRepository<Book, Long> {
				Book findByIsbn(String isbn);
				Page<Book> findByAuthor(String author, Pageable instructions);
				Page<Book> findByPublisher(String publisher, Pageable instructions);
			}

	 * You can put multiple properties in your query method name and separate those properties with logical operators as well:

			List<Person> findByFirstNameAndLastName(String firstName, String lastName);
			List<Person> findByFirstNameOrLastName (String firstName, String lastName);

	 * Many databases ignore case when matching string-based fields (either by default or as an optional configuration), but you can
	 * explicitly indicate that case should be ignored using IgnoreCase:

			Page<Person> findByFirstNameOrLastNameIgnoreCase(String firstName, String lastName, Pageable instructions);

	 * In the preceding example, only the last name ignores case. You can also ignore case on the first name using the method name 
	 * findByFirstNameIgnoreCaseOrLastNameIgnoreCase, but that is very verbose. Instead, you can tell Spring Data to ignore the case
	 * for all String properties using findByFirstNameOrLastNameAllIgnoreCase.
	 * 
	 * Sometimes properties are not simple types. For example, a Person might have an address property of type Address. Spring Data
	 * can also match against this property if the parameter type is Address, but often you don’t want to match on the whole 
	 * address. You may want to return a list of people in a certain postal code, for example. This is easily accomplished using 
	 * Spring Data property expressions.

			List<Person> findByAddressPostalCode(PostalCode code);

	 * Assuming Person has an address property and that property’s type has a postalCode property of type PostalCode, Spring Data 
	 * can find the people in the database with the given postal code. However, property expressions can create ambiguity in the 
	 * matching algorithm. Spring Data greedily matches the property name before looking for a property expression, not unlike a 
	 * regular expression might greedily match an “or more�? control character. The algorithm could match on a different property 
	 * name than you intended, and then fail to find a property within that property’s type matching the property expression. For 
	 * this reason, it’s best to always separate property expressions using an underscore:

			Page<Person> findByAddress_PostalCode(PostalCode code, Pageable instructions);

	 * This removes the ambiguity so that Spring Data matches on the correct property. You undoubtedly remember that method names 
	 * begin with find...By, get...By, or read...By. These are introducing clauses, and By is a delimiter separating the introducing
	 * clause and the criteria to match on. To a large extent, you can place whatever you want to between find, get, or read and By.
	 * For example, to be more “plain language,�? you could name a method findBookByIsbn or findPeopleByFirstNameAndLastName. Book 
	 * and People are ignored in this case. However, if the word Distinct (matching that case) is in the introducing clause (such as
	 * findDistinctBooksByAuthor), this triggers the special behavior of enabling the distinct flag on the underlying query. This 
	 * may or may not apply to the storage medium in use, but for JPA or JdbcTemplate repositories, it’s the equivalent of using the
	 * DISTINCT keyword in the JPQL or SQL query.
	 * 
	 * In addition to the Or and And keywords that separate multiple criteria, the criteria in a query method name can contain many 
	 * other keywords to refine the way the criteria match:

		-- Is and Equals are implied in the absence of other keywords, but you may explicitly use them. For example, findByIsbn is 
		   equivalent to findByIsbnIs and findByIsbnEquals.

		-- Not and IsNot negate any other keyword except for Or and And. Is and Equals are still implied in the absence of other 
		   keywords, so findByIsbnIsNot is equivalent to findByIsbnIsNotEqual.

		-- After and IsAfter indicate that the property is a date and/or time that should come after the given value, whereas Before
		   and IsBefore indicate that the property should come before the given value. Example: findByDateFoundedIsAfter(Date date).

		-- Containing, IsContaining, and Contains indicate that the property’s value may start and end with anything but should 
		   contain the given value. This is similar to StartingWith, IsStartingWith, and StartsWith, which indicate that the 
		   property should start with the specified value. Likewise, EndingWith, IsEndingWith, and EndsWith indicate that the 
		   property should end with the specified value. Example: findByTitleContains(String value) is equivalent to the SQL 
		   criteria WHERE title = '%value%'.

		-- Like is similar to Contains, StartsWith, and EndsWith, except that value you provide should already contain the 
		   appropriate wildcards (instead of Spring Data adding them for you). This gives you the flexibility to specify more 
		   advanced patterns. NotLike simply negates Like. Example: findByTitleLike(String value) could be called with value 
		   "%Catcher%Rye%" and would match “The Catcher in the Rye�? and “Catcher Brings Home Rye Bread.�?

		-- Between and IsBetween indicate that the property should be between the two specified values. This means that you must 
		   provide two parameters for this property criterion. You can use Between on any type that may be compared mathematically 
		   in this manner, such as numeric and date types. Example: findByDateFoundedBetween(Date start, Date end).

		-- Exists indicates that something should exist. Its meaning may vary wildly between storage mediums. It is roughly 
		   equivalent to the EXISTS keyword in JPQL and SQL.

		-- True and IsTrue indicate that the named property should be true, whereas False and IsFalse indicate that the named 
		   property should be false. These keywords do not require method parameters because the value is implied by the keywords 
		   themselves. Example: findByApprovedIsFalse().
		   
		-- GreaterThan and IsGreaterThan indicate that the property is greater than the parameter value. You can include the 
		   parameter value in the bounds with GreaterThanEqual or IsGreaterThanEqual. The inverse of these keywords are LessThan, 
		   IsLessThan, LessThanEqual, and IsLessThanEqual.

		-- In indicates that the property value must be equal to at least one of the values specified. The parameter matching this 
		   criterion should be an Iterable of the same type of the property. Example: findByAuthorIn(Iterable<String> authors).

		-- Null and IsNull indicate that the value should be null. These keywords also do not require method parameters because the
		   value is implied.

		-- Near, IsNear, Within, and IsWithin are keywords useful for certain NoSQL database types but have no useful meaning in JPA.
		-- Regex, MatchesRegex, and Matches indicate that the property value should match the String regular expression (do not use
		   Pattern) specified in the corresponding method parameter.
	 * 
	 */
	UserPrincipal getByUsername(String username);
}
