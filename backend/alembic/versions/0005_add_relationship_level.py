from alembic import op
import sqlalchemy as sa

revision = '0005'
down_revision = '0004'
branch_labels = None
depends_on = None


def upgrade():
    op.add_column('users', sa.Column('relationship_level', sa.Integer(), nullable=False, server_default='0'))


def downgrade():
    op.drop_column('users', 'relationship_level')
