package me.moonsoo.commonmodule.account;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccount is a Querydsl query type for Account
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAccount extends EntityPathBase<Account> {

    private static final long serialVersionUID = -926839929L;

    public static final QAccount account = new QAccount("account");

    public final StringPath authCode = createString("authCode");

    public final StringPath email = createString("email");

    public final BooleanPath emailAuth = createBoolean("emailAuth");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath introduce = createString("introduce");

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath profileImageUri = createString("profileImageUri");

    public final DateTimePath<java.time.ZonedDateTime> regDate = createDateTime("regDate", java.time.ZonedDateTime.class);

    public final SetPath<AccountRole, EnumPath<AccountRole>> roles = this.<AccountRole, EnumPath<AccountRole>>createSet("roles", AccountRole.class, EnumPath.class, PathInits.DIRECT2);

    public final EnumPath<Sex> sex = createEnum("sex", Sex.class);

    public final StringPath username = createString("username");

    public QAccount(String variable) {
        super(Account.class, forVariable(variable));
    }

    public QAccount(Path<? extends Account> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAccount(PathMetadata metadata) {
        super(Account.class, metadata);
    }

}

