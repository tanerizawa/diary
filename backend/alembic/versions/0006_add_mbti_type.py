from alembic import op
import sqlalchemy as sa

revision = '0006'
down_revision = '0005'
branch_labels = None
depends_on = None


def upgrade():
    op.add_column('users', sa.Column('mbti_type', sa.String(), nullable=True))


def downgrade():
    op.drop_column('users', 'mbti_type')
